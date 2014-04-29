package de.lukeslog.alarmclock.ambientService.mail;

import java.util.Date; 
import java.util.Properties; 
import javax.activation.CommandMap; 
import javax.activation.DataHandler; 
import javax.activation.DataSource; 
import javax.activation.FileDataSource; 
import javax.activation.MailcapCommandMap; 
import javax.mail.BodyPart; 
import javax.mail.Multipart; 
import javax.mail.PasswordAuthentication; 
import javax.mail.Session; 
import javax.mail.Transport; 
import javax.mail.internet.InternetAddress; 
import javax.mail.internet.MimeBodyPart; 
import javax.mail.internet.MimeMessage; 
import javax.mail.internet.MimeMultipart; 

import android.util.Log;
 
//completly coppied this from  http://www.jondev.net/articles/Sending_Emails_without_User_Intervention_%28no_Intents%29_in_Android

public class BackgroundMail extends javax.mail.Authenticator 
{ 
	private final String TAG = "clock";
	private String _user; 
  private String _pass; 
 
  private String[] _to; 
  private String _from; 
 
  private String _port; 
  private String _sport; 
 
  private String _host; 
 
  private String _subject; 
  private String _body; 
 
  private boolean _auth; 
   
  private boolean _debuggable; 
 
  private Multipart _multipart; 
 
 
  public BackgroundMail() 
  { 

    _host = "smtp.gmail.com"; // default smtp server 
    _port = "465"; // default smtp port 
    _sport = "465"; // default socketfactory port 
 
    _user = ""; // username 
    _pass = ""; // password 
    _from = ""; // email sent from 
    _subject = ""; // email subject 
    _body = ""; // email body 
 
    _debuggable = false; // debug mode on or off - default off 
    _auth = true; // smtp authentication - default on 
 
    _multipart = new MimeMultipart(); 
 
    // There is something wrong with MailCap, javamail can not find a handler for the multipart/mixed part, so this bit needs to be added. 
    MailcapCommandMap mc = (MailcapCommandMap) CommandMap.getDefaultCommandMap(); 
    mc.addMailcap("text/html;; x-java-content-handler=com.sun.mail.handlers.text_html"); 
    mc.addMailcap("text/xml;; x-java-content-handler=com.sun.mail.handlers.text_xml"); 
    mc.addMailcap("text/plain;; x-java-content-handler=com.sun.mail.handlers.text_plain"); 
    mc.addMailcap("multipart/*;; x-java-content-handler=com.sun.mail.handlers.multipart_mixed"); 
    mc.addMailcap("message/rfc822;; x-java-content-handler=com.sun.mail.handlers.message_rfc822"); 
    CommandMap.setDefaultCommandMap(mc); 
  } 
 
  public BackgroundMail(String user, String pass) 
  { 
    this(); 
 
    Log.i(TAG, "new mail created for "+user);
    _user = user; 
    _pass = pass; 
  } 
 
  public boolean send() throws Exception
  { 
	  Log.i(TAG, "send called");
    Properties props = _setProperties(); 
    if(_user.equals("")) 
    { 
    	Log.i(TAG, "fail 1");
    }
    if(_pass.equals("")) 
    { 
    	Log.i(TAG, "fail 2");
    }
    if(_to.length <= 0) 
    { 
    	Log.i(TAG, "fail 3");
    }
    if(_from.equals("")) 
    { 
    	Log.i(TAG, "fail 4");
    }
    if(_subject.equals("")) 
    { 
    	Log.i(TAG, "fail 5");
    }
    if(_body.equals("")) 
    { 
    	Log.i(TAG, "fail 6");
    }
    if(!_user.equals("") && !_pass.equals("") && _to.length > 0 && !_from.equals("") && !_subject.equals("") && !_body.equals("")) 
    { 
    	Log.i(TAG, "ok try");
      Session session = Session.getInstance(props, this); 
      Log.i(TAG, "2");
      MimeMessage msg = new MimeMessage(session); 
      Log.i(TAG, "3");
      msg.setFrom(new InternetAddress(_from)); 
      Log.i(TAG, "4");
      InternetAddress[] addressTo = new InternetAddress[_to.length]; 
      for (int i = 0; i < _to.length; i++) 
      { 
        addressTo[i] = new InternetAddress(_to[i]); 
      } 
      msg.setRecipients(MimeMessage.RecipientType.TO, addressTo); 
      Log.i(TAG, "5");
      msg.setSubject(_subject); 
      msg.setSentDate(new Date()); 
      Log.i(TAG, "6");
      // setup message body 
      BodyPart messageBodyPart = new MimeBodyPart(); 
      messageBodyPart.setText(_body); 
      _multipart.addBodyPart(messageBodyPart); 
      Log.i(TAG, "7");
      // Put parts in message 
      msg.setContent(_multipart); 
      Log.i(TAG, "8");
      // send email 
      Transport.send(msg); 
      Log.i(TAG, "9");
      return true; 
    } 
    else 
    { 
    	Log.i(TAG, "0b");
      return false; 
    } 
  } 
 
  public void addAttachment(String filename) throws Exception 
  { 
    BodyPart messageBodyPart = new MimeBodyPart(); 
    DataSource source = new FileDataSource(filename); 
    messageBodyPart.setDataHandler(new DataHandler(source)); 
    messageBodyPart.setFileName(filename); 
 
    _multipart.addBodyPart(messageBodyPart); 
  } 
 
  @Override 
  public PasswordAuthentication getPasswordAuthentication() 
  { 
    return new PasswordAuthentication(_user, _pass); 
  } 
 
  private Properties _setProperties() 
  { 
    Properties props = new Properties(); 
 
    props.put("mail.smtp.host", _host); 
 
    if(_debuggable) 
    { 
      props.put("mail.debug", "true"); 
    } 
 
    if(_auth) 
    { 
      props.put("mail.smtp.auth", "true"); 
    } 
 
    props.put("mail.smtp.port", _port); 
    props.put("mail.smtp.socketFactory.port", _sport); 
    props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory"); 
    props.put("mail.smtp.socketFactory.fallback", "false"); 
 
    return props; 
  } 
 
  // the getters and setters 
  public String getBody() 
  { 
    return _body; 
  } 
 
  public void setBody(String _body) 
  { 
    this._body = _body; 
  }  
  
  public void setTo(String[] t)
  {
	  _to = t;
  }
  
  public void setFrom(String f)
  {
	  _from = f;
  }
  
  public void setSubject(String subj)
  {
	  _subject = subj;
  }
}
