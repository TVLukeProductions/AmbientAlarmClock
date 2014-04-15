package de.jaetzold.networking;

/*
 Copyright (c) 2013 Stephan Jaetzold.

 Licensed under the Apache License, Version 2.0 (the "License");
 You may not use this file except in compliance with the License.
 You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and limitations under the License.
 */

import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;

import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.conn.util.InetAddressUtils;

import android.util.Log;

import de.jaetzold.philips.hue.HueBridge;

/**
 * Implementation of the Simple Service Discovery Protocol (SSDP) part that discovers devices.
 * The goal is to have no dependencies and provide device detection without the API user needing to know more than necessary about UPnP/SSDP.
 * So all values have defaults and a simple <code>new SimpleServiceDiscovery().discover()</code> suffices to get a list of all UPnP devices
 * in the current network.
 * To search only for UPnP root devices use <code>discover(SimpleServiceDiscovery.SEARCH_TARGET_ROOTDEVICE)</code>.
 *
 * @author Stephan Jaetzold <p><small>Created at 15.03.13, 16:34</small>
 */
@SuppressWarnings("UnusedDeclaration")
public class SimpleServiceDiscovery 
{
	static final int MULTICAST_PORT = 1900;
	static final String MULTICAST_ADDRESS = "239.255.255.250";
	static final String RESPONSE_MESSAGE_HEADER = "HTTP/1.1 200 OK\r\n";

	static final String CHARSET_NAME = "UTF-8";
	static final Pattern HEADER_PATTERN = Pattern.compile("([^\\p{Cntrl} :]+):\\s*(.*)");

	/**
	 * The search target to discover all devices.
	 */
	public static final String SEARCH_TARGET_ALL = "ssdp:all";
	/**
	 * The search target to discover only UPnP root devices.
	 */
	public static final String SEARCH_TARGET_ROOTDEVICE = "upnp:rootdevice";

	int searchMx = 2;	// maximum response wait time
	String searchTarget = SEARCH_TARGET_ALL;
	protected String userAgentProduct = SimpleServiceDiscovery.class.getName();
	protected String userAgentProductVersion = "0.1";

	final PrintStream debugLog;

	// http://www.upnp.org/specs/arch/UPnP-arch-DeviceArchitecture-v1.1.pdf, Section 1, Page 15, Paragraph 3
	// http://www.upnp.org/specs/arch/UPnP-arch-DeviceArchitecture-v1.1.pdf, Section 1.3.2, Page 30, Paragraph 4
	int ttl = 5;	// this SHOULD be configurable
	int socketTimeout = searchMx*1000+2000;

	public SimpleServiceDiscovery() {
		this(null);
	}

	/**
	 * @param debugLog a PrintStream where debug loggings with e.g. the received packet contents are sent to.
	 */
	public SimpleServiceDiscovery(PrintStream debugLog) {
		this.debugLog = debugLog;
	}

	/**
	 * The maximum time a device should delay sending its answer in seconds.
	 */
	public int getSearchMx() {
		return searchMx;
	}

	/**
	 * @param searchMx The maximum time a device should delay sending its answer in seconds. Defaults to 2.
	 */
	public void setSearchMx(int searchMx) {
		final int timeout = getSocketTimeout();
		this.searchMx = searchMx;
		setSocketTimeout(timeout);
	}

	/**
	 * The current value for the the "ST" field of the search message.
	 */
	public String getSearchTarget() {
		return searchTarget;
	}

	/**
	 * What to include in the "ST" field of the search message.
	 * See <a href="http://www.upnp.org/specs/arch/UPnP-arch-DeviceArchitecture-v1.1.pdf">UPnP Device Architecture 1.1</a>,
	 * page 31 for allowed values.
	 * Or use one of the predefined constants {@link #SEARCH_TARGET_ALL} and {@link #SEARCH_TARGET_ROOTDEVICE}.
	 * Defaults to {@link #SEARCH_TARGET_ALL}.
	 *
	 * @param searchTarget What to include in the "ST" field of the search message.
	 */
	public void setSearchTarget(String searchTarget) {
		this.searchTarget = searchTarget;
	}

	/**
	 * The time to live used for the multicast discovery message.
	 */
	public int getTimeToLive() {
		return ttl;
	}

	/**
	 * The time to live of the multicast discovery message. Defaults to 2.
	 *
	 * @param ttl The time to live of the multicast discovery message.
	 */
	public void setTimeToLive(int ttl) {
		this.ttl = ttl;
	}

	/**
	 * The time (in milliseconds) to wait for additional responses <em>after</em> {@link #searchMx} seconds have passed.
	 */
	public int getSocketTimeout() {
		return socketTimeout-(searchMx*1000);
	}

	/**
	 * Set the time (in milliseconds) to wait for additional responses <em>after</em> {@link #searchMx} seconds have passed.
	 * Defaults to 2000.
	 *
	 * @param socketTimeout the time (in milliseconds) to wait
	 */
	public void setSocketTimeout(int socketTimeout) {
		this.socketTimeout = searchMx*1000+socketTimeout;
	}

	/**
	 * Send a SSDP search message and return a list of received responses.
	 */
	public Map<String, URL> discover() {
		return discover(searchTarget);
	}

	/**
	 * Send a SSDP search message with the given search target (ST) and return a list of received responses.
	 */
	public Map<String, URL> discover(String searchTarget) 
	{
		Log.d("HUE", "ServiceDiscovery.discover");
		final InetSocketAddress address;
		// standard multicast port for SSDP
		try 
		{
			// multicast address with administrative scope
			address = new InetSocketAddress(InetAddress.getByName(MULTICAST_ADDRESS), MULTICAST_PORT);
			//address = InetAddress.getByName(MULTICAST_ADDRESS);
			 
		} 
		catch(UnknownHostException e) 
		{
			e.printStackTrace();
			throw new IllegalStateException("Can not get multicast address", e);
		}

		final MulticastSocket socket;
		try 
		{
			socket = new MulticastSocket(null);
			
			InetAddress localhost = getAndroidLocalIP();
			
			InetSocketAddress srcAddress = new InetSocketAddress(localhost, MULTICAST_PORT);
			Log.d("HUE", ""+srcAddress.getAddress());
			socket.bind(srcAddress);
			Log.d("HUE", "step 1");
		} 
		catch(IOException e) 
		{
			e.printStackTrace();
			throw new IllegalStateException("Can not create multicast socket");
		}

		try 
		{
			socket.setSoTimeout(socketTimeout);
			Log.d("HUE", "step 2");
		} 
		catch(SocketException e) 
		{
			e.printStackTrace();
			throw new IllegalStateException("Can not set socket timeout", e);
		}

		try 
		{
			socket.joinGroup(InetAddress.getByName(MULTICAST_ADDRESS));
			Log.d("HUE", "step 3");
		} 
		catch(IOException e) 
		{
			e.printStackTrace();
			throw new IllegalStateException("Can not make multicast socket joinGroup " +address, e);
		}
		try 
		{
			socket.setTimeToLive(ttl);
			Log.d("HUE", "step 4");
		} 
		catch(IOException e) 
		{
			e.printStackTrace();
			throw new IllegalStateException("Can not set TTL " + ttl, e);
		}
		final byte[] transmitBuffer;
		try 
		{
			transmitBuffer = constructSearchMessage(searchTarget).getBytes(CHARSET_NAME);
			Log.d("HUE", "step 5");
		} 
		catch(UnsupportedEncodingException e) 
		{
			e.printStackTrace();
			throw new IllegalStateException("WTF? " +CHARSET_NAME +" is not supported?", e);
		}
		DatagramPacket packet = null;
		try 
		{
			packet = new DatagramPacket(transmitBuffer, transmitBuffer.length, address);
			Log.d("HUE", "step 6");
		} 
		catch (SocketException e1) 
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try 
		{
			socket.send(packet);
			Log.d("HUE", "step 7");
		} 
		catch(IOException e) 
		{
			e.printStackTrace();
			throw new IllegalStateException("Can not send search request", e);
		}
		Map<String, URL> result = new HashMap<String, URL>();
		byte[] receiveBuffer = new byte[1536];
		while(true) 
		{
			try 
			{
				Log.d("HUE", "sending packets");
				final DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length, InetAddress.getByName(MULTICAST_ADDRESS), MULTICAST_PORT);
				socket.receive(receivePacket);
				//Log.d("HUE", new String(receivePacket.getData()));
				HueBridge response = parseResponsePacket(receivePacket);
				if(response!=null) 
				{
					Log.d("HUE", "resonse not null");
					////System.out.println("resonse not null");
					result.put(response.getUDN(), response.getBaseUrl());
				} 
				else 
				{
					Log.d("HUE", "no bridge");
				}
			}
			catch(SocketTimeoutException e) 
			{
				Log.e("HUE", "timeout exception");
				break;
			} 
			catch(IOException e) 
			{
				throw new IllegalStateException("Problem receiving search responses", e);
			}
		} 
		return result;
	}

	private InetAddress getAndroidLocalIP() 
	{
		String ipv4 = getIPAddress(true);
		StringTokenizer tk = new StringTokenizer(ipv4, ".");
		int a=0;
		int b=0;
		int c=0;
		int d=0;
		try
		{
			a = Integer.parseInt(tk.nextToken());
			b = Integer.parseInt(tk.nextToken());
			c = Integer.parseInt(tk.nextToken());
			d = Integer.parseInt(tk.nextToken());
		}
		catch(Exception e)
		{
			try 
			{
				return InetAddress.getByAddress(new byte[] {(byte)a,(byte)b,(byte)c,(byte)d});
			} 
			catch (UnknownHostException e1) 
			{
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		try 
		{
			return InetAddress.getByAddress(new byte[] {(byte)a,(byte)b,(byte)c,(byte)d});
		} 
		catch (UnknownHostException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	/**
     * Comma separated List of IP adresses of available network interfaces
     * @param useIPv4
     * @return
     */
    public static String getIPAddress(boolean useIPv4) 
    {

        String addresses = "";
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                for (InetAddress addr : addrs) {
                    if (!addr.isLoopbackAddress()) {
                        String sAddr = addr.getHostAddress().toUpperCase();
                        boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr);
                        if (useIPv4) {
                            if (isIPv4)
                                addresses += sAddr + ", ";
                        } else {
                            if (!isIPv4) {
                                int delim = sAddr.indexOf('%'); // drop ip6 port suffix
                                if(delim<0) addresses += sAddr + ", ";
                                else addresses += sAddr.substring(0, delim) + ", ";
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) { } // for now eat exceptions
        if(addresses == null || addresses.length() <= 3) return "";
        return addresses.subSequence(0, addresses.length()-2).toString();
    }


	private HueBridge parseResponsePacket(DatagramPacket receivePacket) 
	{
		Log.d("HUE", "parse resonse");
		HueBridge bridge = null;
		String message = new String(receivePacket.getData());
        if(message.contains("/description.xml"))
        {
        	//System.out.println("there is a description.xml, lets get it");
        	InetAddress address = receivePacket.getAddress();
        	bridge = new HueBridge(address, "xx26xxb3xx10xx0fxx5bxxabxx7bxx57");
        }
        return bridge;
	}

	String constructSearchMessage(String searchTarget) 
	{
		return "M-SEARCH * HTTP/1.1\r\nMAN: ssdp:discover\r\nMX: 10\r\nHOST: 239.255.255.250:1900\r\nST: ssdp:all\r\n";
	}


}