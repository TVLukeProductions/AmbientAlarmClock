package de.jaetzold.philips.hue;

/*
 Copyright (c) 2013 Stephan Jaetzold.

 Licensed under the Apache License, Version 2.0 (the "License");
 You may not use this file except in compliance with the License.
 You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and limitations under the License.
 */

import org.json2.JSONArray;
import org.json2.JSONObject;
import org.json2.JSONStringer;
import org.json2.JSONWriter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static de.jaetzold.philips.hue.HueBridgeComm.RM.*;
import static de.jaetzold.philips.hue.HueLight.ColorMode.*;

/**
 * This class represents a single light bulb. Use it to query or manipulate the state of a single light bulb.
 * <p>
 *     An instance of this class is not created directly. Instead query a {@link HueBridge} for its lights using either
 *     {@link HueBridge#getLights()} or {@link HueBridge#getLight(Integer)}.
 * </p>
 * <p>
 *     When querying for state the actual value is automatically updated with the current value on the bridge if its local cache is 'too old'.
 *     This behaviour can be tuned (or turned off) using {@link #setAutoSyncInterval(Integer)}.
 * </p>
 *
 * As a general note: A state value here is always only a cached version that may already be incorrect.
 * Even if no one else is controlling the lights. I've observed that e.g. the brightness value changes if a light is just switched on.
 *
 * <p>See <a href="http://developers.meethue.com/1_lightsapi.html">Philips hue API, Section 1</a> for further reference.</p>
 *
 * @author Stephan Jaetzold <p><small>Created at 20.03.13, 14:59</small>
 */
public class HueLightBulb implements HueLight, Comparable<HueLightBulb> {
	final Integer id;
	final HueBridge bridge;

	String name;

	boolean on;
	int brightness;
	int hue;
	int saturation;
	double ciex;
	double ciey;
	int colorTemperature;
	Effect effect;

	ColorMode colorMode;
	Integer transitionTime;

	Integer autoSyncInterval = 1000;
	long lastSyncTime;

	/**
	 * This constructor is package private, since lights are not to be created. A {@link HueBridge} is queried for them.
	 */
	public HueLightBulb(HueBridge bridge, Integer id) {
		if(id==null || id<0) {
			throw new IllegalArgumentException("id has to be non-negative and non-null");
		}
		if(bridge==null) {
			throw new IllegalArgumentException("bridge may not be null");
		}
		this.bridge = bridge;
		this.id = id;
	}

	@Override
	public Integer getId() {
		return id;
	}

	@Override
	public HueBridge getBridge() {
		return bridge;
	}

	@Override
	public Integer getTransitionTime() {
		return transitionTime;
	}

	@Override
	public void setTransitionTime(Integer transitionTime) {
		this.transitionTime = transitionTime;
	}

	/**
	 * The time in milliseconds that a queried state value may be old before it needs to be updated with the current value from the bridge device.
	 *
	 * @return null, if values are never automatically updated with the current state from the bridge device.
	 */
	public Integer getAutoSyncInterval() {
		return autoSyncInterval;
	}

	/**
	 * Set the time in milliseconds that a queried state value may be old before it needs to be updated with the current value from the bridge device.
	 * This may be null if not automatic syncing should take place. Manual syncing can be done by calling {@link #sync()}.
	 * The default value is 1000 milliseconds.
	 *
	 * @param autoSyncInterval The time in milliseconds that a cached state value is used until it is updated again.
	 */
	public void setAutoSyncInterval(Integer autoSyncInterval) {
		this.autoSyncInterval = autoSyncInterval;
	}

	@Override
	public String getName() {
		checkSync();
		return name;
	}

	@Override
	public void setName(String name) {
		if(name==null || name.trim().length()>32) {
			throw new IllegalArgumentException("Name (without leading or trailing whitespace) has to be less than 32 characters long");
		}
		final JSONObject response = bridge.checkedSuccessRequest(PUT, "/lights/" +id, JO().key("name").value(name.trim())).get(0);
		final String actualName = response.getJSONObject("success").optString("/lights/" + id + "/name");
		this.name = actualName!=null ? actualName : name;
	}

	/**
	 * Whether this light is in 'on' or 'off' state.
	 *
	 * @return true if this light is on, false otherwise.
	 */
	public boolean isOn() {
		checkSync();
		return on;
	}

	
	@Override
	public void setState(Boolean on, Integer hue, Integer brightness, Integer saturation) {
		if(brightness<0 || brightness>255) 
		{
			throw new IllegalArgumentException("Brightness must be between 0-255");
		}
		if(hue<0 || hue>65535) 
		{
			throw new IllegalArgumentException("Hue must be between 0-65535");
		}
		if(saturation<0 || saturation>255) 
		{
			throw new IllegalArgumentException("Saturation must be between 0-255");
		}

		Map<String, Object> state = new HashMap<String, Object>();
		
		state.put("on", on);
		state.put("hue", hue);
		state.put("bri", brightness);
		state.put("sat", saturation);
		state.put("effect", Effect.NONE);
		state.put("alert", Alert.NONE);
		
		stateChange(state);
		
		this.on = on;
		this.hue = hue;
		this.brightness = brightness;
		this.saturation = saturation;	
		this.colorMode = ColorMode.HS;
		this.effect = Effect.NONE;

	}
	
	
	public Map<String,Object> getStateKeyValueMap() {
		Map<String,Object> map = new HashMap<String,Object>();
		
		//map.put("id", id);
		//map.put("name", name);
		
		map.put("on", on);
		map.put("hue", hue);
		map.put("brightness", brightness);
		map.put("saturation", saturation);
		//map.put("colorTemperature", colorTemperature);
		//map.put("xy", "["+ciex+","+ciey+"]");
		//map.put("colorMode", colorMode);
		map.put("effect", effect);

		return map;
	}
	
	public void setStateUsingKeyValueMap(Map<String,String> new_state) {
		Map<String,Object> new_state_converted = convert(new_state);
		stateChange(new_state_converted);
		setStateFromMap(new_state_converted);
	}
	
	
	private Map<String,Object> convert(Map<String,String> map) {
		Map<String,Object> new_map = new HashMap<String,Object>();
		
		int rgb_color = 0;
		int hsv_color = 0;
		int name_color = 0;
		
		for (String key : map.keySet()) {
			String new_key = key;
			if (key.equals("brightness")) {
				hsv_color = 1;
				new_key = "bri";
				Integer new_value = new Integer(map.get(key));
				int v = ((Integer)new_value).intValue();
				if(v<0 || v>255) {
					throw new IllegalArgumentException("Brightness must be between 0-255");
				}
				new_map.put(new_key, new_value);
				
			} else if (key.equals("saturation")) {
				hsv_color = 1;
				new_key = "sat";
				Integer new_value = new Integer(map.get(key));
				int v = ((Integer)new_value).intValue();
				if(v<0 || v>255) {
					throw new IllegalArgumentException("Saturation must be between 0-255");
				}
				new_map.put(new_key, new_value);
				
			} else if (key.equals("colorTemperature")) {
				new_key = "ct";
				Integer new_value = new Integer(map.get(key));
				int v = ((Integer)new_value).intValue();
				if(v<153 || v>500) {
					throw new IllegalArgumentException("Color Temperature must be between 153-500");
				}
				new_map.put(new_key, new_value);
				
				
			} else if (key.equals("hue")) {	
				hsv_color = 1;
				new_key = "hue";
				Integer new_value = new Integer(map.get(key));
				int v = ((Integer)new_value).intValue();
				if(v<0 || v>65535) {
					throw new IllegalArgumentException("Hue must be between 0-65535");
				}
				new_map.put(new_key, new_value);
				
			} else if (key.equals("transitiontime")) {	
				new_key = "transitiontime";
				Integer new_value = new Integer(map.get(key));
				int v = ((Integer)new_value).intValue();
				if(v<0 || v>65535) {
					throw new IllegalArgumentException("Transitiontime must be between 0-65535");
				}
				new_map.put(new_key, new_value);
			
			} else if (key.equals("on")) {	
				new_key = "on";
				Boolean new_value = new Boolean(map.get(key));
		
				new_map.put(new_key, new_value);
				
			} else if ((key.equals("effect")) || (key.equals("alarm"))) {
				new_map.put(new_key, map.get(key));
				
			} else if (key.equals("rgb")) {
				rgb_color = 1;
				new_key = "rgb";
				new_map.put(new_key, map.get(key));
				
			} else if (key.equals("colorname")) {
				name_color = 1;
				new_key = "colorname";
				new_map.put(new_key, map.get(key));
			}
		}
		
		if (rgb_color+hsv_color+name_color>1) {
			throw new IllegalArgumentException("Please use only one color information at a time (hue,saturation,brightness OR rgb OR colorname)");
		}
		
		
		if (new_map.containsKey("colorname")) {
			String colorname = (String)new_map.get("colorname");
			new_map.remove("colorname");
			String rgb = ColorHelper.convertName2RGB(colorname);
			if (rgb == null) {
				throw new IllegalArgumentException("No color with name "+colorname+" found!");
			}
			new_map.put("rgb", rgb);
		}
		if (new_map.containsKey("rgb")) {
			String rgbcolor = (String)new_map.get("rgb");
			new_map.remove("rgb");
			new_map.remove("hue");
			new_map.remove("sat");
			new_map.remove("bri");
			Map<String,Integer> hue = ColorHelper.convertRGB2Hue(rgbcolor);
			new_map.putAll(hue);
		}
		
		return new_map;
	}
	

	
	private void setStateFromMap(Map<String,Object> map) {
		for (String key : map.keySet()) {
			Object value = map.get(key);
			
			if (key.equals("bri")) {
				this.brightness = (Integer) value;
			} else if (key.equals("sat")) {
				this.saturation = (Integer) value;
			} else if (key.equals("hue")) {
				this.hue = (Integer) value;
				
			} else if (key.equals("on")) {
				this.on =  (Boolean) value;
				
			} else if (key.equals("effect")) {
				this.effect = Effect.fromName((String)value);
			}
			colorMode = ColorMode.HS;
		}
	}
	
	
	/**
	 * Whether this light is in 'on' or 'off' state.
	 *
	 * @return true if this light is on, false otherwise.
	 */
	public Boolean getOn() {
		return isOn();
	}

	@Override
	public void setOn(Boolean on) {
		stateChange("on", on);
		this.on = on;
	}

	/**
	 * Get the brightness of this light.
	 *
	 * <p>See <a href="http://developers.meethue.com/1_lightsapi.html#14_get_light_attributes_and_state">Philips hue API, Section 1.4</a> for further reference.</p>
	 *
	 * @return the brightness value between 0 (lowest that is not off) and 255 (highest)
	 */
	public Integer getBrightness() {
		checkSync();
		return brightness;
	}

	@Override
	public void setBrightness(Integer brightness) {
		if(brightness<0 || brightness>255) {
			throw new IllegalArgumentException("Brightness must be between 0-255");
		}
		stateChange("bri", brightness);
		this.brightness = brightness;
	}

	/**
	 * Get the hue of this light. Note that the validity of this value may depend on {@link #getColorMode()}.
	 *
	 * <p>See <a href="http://developers.meethue.com/1_lightsapi.html#14_get_light_attributes_and_state">Philips hue API, Section 1.4</a> for further reference.</p>
	 *
	 * @see #HUE_RED
	 * @see #HUE_GREEN
	 * @see #HUE_BLUE
	 *
	 * @return the hue value between 0 and 65535 (both red)
	 */
	public Integer getHue() {
		checkSync();
		return hue;
	}

	@Override
	public void setHue(Integer hue) {
		if(hue<0 || hue>65535) {
			throw new IllegalArgumentException("Hue must be between 0-65535");
		}
		stateChange("hue", hue);
		this.hue = hue;
		colorMode = ColorMode.HS;
	}

	/**
	 * Get the saturation of this light. Note that the validity of this value may depend on {@link #getColorMode()}.
	 *
	 * <p>See <a href="http://developers.meethue.com/1_lightsapi.html#14_get_light_attributes_and_state">Philips hue API, Section 1.4</a> for further reference.</p>
	 *
	 * @return the saturation value between 0 (white) and 255 (colored)
	 */
	public Integer getSaturation() {
		checkSync();
		return saturation;
	}

	@Override
	public void setSaturation(Integer saturation) {
		if(saturation<0 || saturation>255) {
			throw new IllegalArgumentException("Saturation must be between 0-255");
		}
		stateChange("sat", saturation);
		this.saturation = saturation;
		colorMode = ColorMode.HS;
	}

	/**
	 * Get the x coordinate in CIE color space of this light. Note that the validity of this value may depend on {@link #getColorMode()}.
	 *
	 * <p>See <a href="http://developers.meethue.com/1_lightsapi.html#14_get_light_attributes_and_state">Philips hue API, Section 1.4</a> for further reference.</p>
	 *
	 * @return the x coordinate in CIE color space between 0 and 1
	 */
	public Double getCiex() {
		checkSync();
		return ciex;
	}

	/**
	 * Set the x coordinate of a color in CIE color space. For y the currently cached value is used.
	 *
	 * <p>See <a href="http://developers.meethue.com/1_lightsapi.html#16_set_light_state">Philips hue API, Section 1.6</a> for further reference.</p>
	 *
	 * @param ciex the x coordinate in CIE color space between 0 and 1
	 */
	public void setCiex(Double ciex) {
		setCieXY(ciex, ciey);
		this.ciex = ciex;
	}

	/**
	 * Get the y coordinate in CIE color space of this light. Note that the validity of this value may depend on {@link #getColorMode()}.
	 *
	 * <p>See <a href="http://developers.meethue.com/1_lightsapi.html#14_get_light_attributes_and_state">Philips hue API, Section 1.4</a> for further reference.</p>
	 *
	 * @return the y coordinate in CIE color space between 0 and 1
	 */
	public Double getCiey() {
		checkSync();
		return ciey;
	}

	/**
	 * Set the y coordinate of a color in CIE color space. For x the currently cached value is used.
	 *
	 * <p>See <a href="http://developers.meethue.com/1_lightsapi.html#16_set_light_state">Philips hue API, Section 1.6</a> for further reference.</p>
	 *
	 * @param ciey the y coordinate in CIE color space between 0 and 1
	 */
	public void setCiey(Double ciey) {
		setCieXY(ciex, ciey);
		this.ciey = ciey;
	}

	@Override
	public void setCieXY(Double ciex, Double ciey) {
		if(ciex<0 || ciex>1 || ciey<0 || ciey>1) {
			throw new IllegalArgumentException("A cie coordinate must be between 0.0-1.0");
		}
		stateChange("xy", new JSONArray(Arrays.asList(ciex.floatValue(),ciey.floatValue())));
		this.ciex = ciex;
		this.ciey = ciey;
		colorMode = ColorMode.XY;
	}

	/**
	 * Get the mired color temperature of this light. Note that the validity of this value may depend on {@link #getColorMode()}.
	 *
	 * <p>See <a href="http://developers.meethue.com/1_lightsapi.html#14_get_light_attributes_and_state">Philips hue API, Section 1.4</a> for further reference.</p>
	 *
	 * @return the color temperature value in mired between 153 (6500K) and 500 (2000K)
	 */
	public Integer getColorTemperature() {
		checkSync();
		return colorTemperature;
	}

	@Override
	public void setColorTemperature(Integer colorTemperature) {
		if(colorTemperature<153 || colorTemperature>500) {
			throw new IllegalArgumentException("ColorTemperature must be between 153-500");
		}
		stateChange("ct", colorTemperature);
		this.colorTemperature = colorTemperature;
		colorMode = ColorMode.CT;
	}

	/**
	 * Get the current dynamic effect of this light.
	 *
	 * <p>See <a href="http://developers.meethue.com/1_lightsapi.html#14_get_light_attributes_and_state">Philips hue API, Section 1.4</a> for further reference.</p>
	 *
	 * @return the current dynamic effect of this light.
	 */
	public Effect getEffect() {
		checkSync();
		return effect;
	}

	@Override
	public void setEffect(Effect effect) {
		stateChange("effect", effect.name);
		this.effect = effect;
	}

	@Override
	public void setAlert(Alert alert) {
		stateChange("alert", alert.name);
	}

	/**
	 * Get the mode with which the current color of the light has been set.
	 *
	 * <p>See <a href="http://developers.meethue.com/1_lightsapi.html#14_get_light_attributes_and_state">Philips hue API, Section 1.4</a> for further reference.</p>
	 *
	 * @return The current mode with which the current color of the light has been set.
	 */
	public ColorMode getColorMode() {
		return colorMode;
	}

	@Override
	public String toString() {
		return getId() +"(" +getName() +")" +"["
			   +(isOn() ? "ON" : "OFF") +","
			   +(getColorMode()==ColorMode.CT ? "CT:"+getColorTemperature() : "")
			   +(getColorMode()==ColorMode.HS ? "HS:"+getHue() +"/" +getSaturation() : "")
			   +(getColorMode()==ColorMode.XY ? "XY:"+getCiex() +"/" +getCiey() : "")
			   + ","
			   +"BRI:" +getBrightness()
			   +(getEffect()!=Effect.NONE ? ","+getEffect() : "")
			   +"]";
	}

	@Override
	public void stateChangeTransaction(Integer transitionTime, Runnable changes) {
		openStateChangeTransaction(transitionTime);
		try {
			try {
				changes.run();
			} catch(Throwable t) {
				stateTransactionJson.set(null);
				//noinspection ThrowCaughtLocally
				throw t;
			} finally {
				commitStateChangeTransaction();
			}
		} catch(Throwable t) {
			// do kind of rollback by syncing the state from the bridge
			sync();
		}
	}

	/**
	 * Update the local state cache with values from the bridge device.
	 */
	public void sync() {
		if(syncing.get() == null || !syncing.get()) {
			try {
				syncing.set(true);
				final JSONObject response = bridge.request(GET, "/lights/" + getId(), "").get(0);
				if(response.has("error")) {
					throw new HueCommException(response.getJSONObject("error"));
				} else {
					parseLight(response);
				}
			} finally {
				syncing.set(false);
			}
		}
	}

	// *****************************************
	// Implementation internal methods
	// *****************************************

	private ThreadLocal<JSONObject> stateTransactionJson = new ThreadLocal<JSONObject>();
	private void openStateChangeTransaction(Integer transitionTime) {
		if(stateTransactionJson.get()==null) {
			stateTransactionJson.set(new JSONObject());
			if(transitionTime!=null) {
				stateTransactionJson.get().put("transitiontime", transitionTime);
			}
		} else {
			throw new IllegalStateException("Have an open state change transaction already");
		}
	}

	private List<JSONObject> commitStateChangeTransaction() {
		final JSONObject json = stateTransactionJson.get();
		stateTransactionJson.set(null);
		if(json!=null) {
			return bridge.checkedSuccessRequest(PUT, "/lights/" + getId() + "/state", json);
		} else {
			return null;
		}
	}

	private List<JSONObject> stateChange(String param, Object value) {
		Map<String,Object> map = new HashMap<String,Object>();
		map.put(param, value);
		return stateChange(map);		
	}
	
	private List<JSONObject> stateChange(Map<String,Object> param_value) {
		if(param_value==null) {
			throw new IllegalArgumentException("A value of null is not allowed for any of the lights states");
		}
		final JSONObject stateTransaction = stateTransactionJson.get();
		if(stateTransaction==null) {
			JSONWriter json = JO();
			if(transitionTime!=null) {
				json = json.key("transitiontime").value(transitionTime);
			}
			for (String s : param_value.keySet()) {
				json.key(s).value(param_value.get(s));
			}
			return bridge.checkedSuccessRequest(PUT, "/lights/" + getId() + "/state", json);
		} else {
			for (String s : param_value.keySet()) {
				stateTransaction.put(s, param_value.get(s));
			}
			return null;
		}
	}

	private void checkSync() {
		final long now = System.currentTimeMillis();
		if(autoSyncInterval!=null && now-lastSyncTime>autoSyncInterval) {
			sync();
		}
	}

	private ThreadLocal<Boolean> syncing = new ThreadLocal<Boolean>();

	void parseLight(JSONObject lightJson) {
		name = lightJson.getString("name");

		if(lightJson.has("state")) {
			final JSONObject state = lightJson.getJSONObject("state");
			on = state.getBoolean("on");
			brightness = state.getInt("bri");
			hue = state.getInt("hue");
			saturation = state.getInt("sat");
			ciex = state.getJSONArray("xy").getDouble(0);
			ciey = state.getJSONArray("xy").getDouble(1);
			colorTemperature = state.getInt("ct");
			colorMode = new ColorMode[]{HS,XY,CT}[Arrays.asList("hs", "xy", "ct").indexOf(state.getString("colormode").toLowerCase())];
			final Effect effect = Effect.fromName(state.getString("effect"));
			if(effect==null) {
				throw new HueCommException("Can not find effect named \"" +state.getString("effect") +"\"");
			}
			this.effect = effect;

			lastSyncTime = System.currentTimeMillis();
		} else {
			sync();
		}
	}

	/**
	 * Helper method to shorten creation of a JSONObject String.
	 * @return A JSONStringer with an object already 'open' and auto-object-end on a call to toString()
	 */
	private static JSONStringer JO() {
		return new JSONStringer() {
			{ object(); }
			@Override
			public String toString() {
				return writer.toString()+(mode!='d' ? "}" :"");
			}
		};
	}

	@Override
	public int compareTo(HueLightBulb other) {
		if (this.id==other.id) {
			if (this.name.equals(other.name)) {
				
				if ((this.on==other.on) &&
						(this.hue==other.hue) &&
						(this.brightness==other.brightness) &&
						(this.saturation==other.saturation) &&
						(this.colorTemperature==other.colorTemperature) &&
						(this.ciex==other.ciex) &&
						(this.ciey==other.ciey) &&
						(this.colorMode==other.colorMode) &&
						(this.effect==other.effect)) {
					return 0;
				} else {
					return 1;
				}
				
				
			} else {
				return this.name.compareTo(other.name);
			}
		} else {
			return this.id.compareTo(other.id);
		}		
	}
}
