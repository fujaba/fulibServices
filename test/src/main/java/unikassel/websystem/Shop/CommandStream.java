package unikassel.websystem.Shop;

import org.fulib.yaml.Yaml;
import spark.Request;
import spark.Response;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;

import static spark.Spark.post;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyChangeListener;
import java.io.*;
import java.util.*;
import spark.Service;

public class CommandStream  
{
   private ShopService service = null;
   private java.util.Map<String, ModelCommand> activeCommands = new java.util.LinkedHashMap<>();
   private ArrayList<ModelCommand> oldCommands = new ArrayList<>();

   public ArrayList<ModelCommand> getOldCommands()
   {
      return oldCommands;
   }

   public CommandStream subscribe(ShopService service) {
      this.service = service;
      return this;
   }
   public static final String PROPERTY_service = "service";

   public ShopService getService()
   {
      return this.service;
   }

   public CommandStream setService(ShopService value)
   {
      if (this.service != value)
      {
         ShopService oldValue = this.service;
         if (this.service != null)
         {
            this.service = null;
            oldValue.withoutStreams(this);
         }
         this.service = value;
         if (value != null)
         {
            value.withStreams(this);
         }
         firePropertyChange("service", oldValue, value);
      }
      return this;
   }

   protected PropertyChangeSupport listeners = null;

   public boolean firePropertyChange(String propertyName, Object oldValue, Object newValue)
   {
      if (listeners != null)
      {
         listeners.firePropertyChange(propertyName, oldValue, newValue);
         return true;
      }
      return false;
   }

   public boolean addPropertyChangeListener(PropertyChangeListener listener)
   {
      if (listeners == null)
      {
         listeners = new PropertyChangeSupport(this);
      }
      listeners.addPropertyChangeListener(listener);
      return true;
   }

   public boolean addPropertyChangeListener(String propertyName, PropertyChangeListener listener)
   {
      if (listeners == null)
      {
         listeners = new PropertyChangeSupport(this);
      }
      listeners.addPropertyChangeListener(propertyName, listener);
      return true;
   }

   public boolean removePropertyChangeListener(PropertyChangeListener listener)
   {
      if (listeners != null)
      {
         listeners.removePropertyChangeListener(listener);
      }
      return true;
   }

   public boolean removePropertyChangeListener(String propertyName,PropertyChangeListener listener)
   {
      if (listeners != null)
      {
         listeners.removePropertyChangeListener(propertyName, listener);
      }
      return true;
   }

   public void removeYou()
   {
      this.setService(null);

   }

   public static final String PROPERTY_activeCommands = "activeCommands";

   public java.util.Map<String, ModelCommand> getActiveCommands()
   {
      return activeCommands;
   }

   public CommandStream setActiveCommands(java.util.Map<String, ModelCommand> value)
   {
      if (value != this.activeCommands)
      {
         java.util.Map<String, ModelCommand> oldValue = this.activeCommands;
         this.activeCommands = value;
         firePropertyChange("activeCommands", oldValue, value);
      }
      return this;
   }

   public static final String PROPERTY_oldCommands = "oldCommands";

   public CommandStream setOldCommands(ArrayList<ModelCommand> value)
   {
      if (value != this.oldCommands)
      {
         ArrayList<ModelCommand> oldValue = this.oldCommands;
         this.oldCommands = value;
         firePropertyChange("oldCommands", oldValue, value);
      }
      return this;
   }

   public static final String PROPERTY_name = "name";

   private String name;

   public String getName()
   {
      return name;
   }

   public CommandStream setName(String value)
   {
      if (value == null ? this.name != null : ! value.equals(this.name))
      {
         String oldValue = this.name;
         this.name = value;
         firePropertyChange("name", oldValue, value);
      }
      return this;
   }

   public static final String PROPERTY_targetUrlList = "targetUrlList";

   private ArrayList<String> targetUrlList = new ArrayList<>();

   public ArrayList<String> getTargetUrlList()
   {
      return targetUrlList;
   }

   public CommandStream setTargetUrlList(ArrayList<String> value)
   {
      if (value != this.targetUrlList)
      {
         ArrayList<String> oldValue = this.targetUrlList;
         this.targetUrlList = value;
         firePropertyChange("targetUrlList", oldValue, value);
      }
      return this;
   }

   @Override
   public String toString()
   {
      StringBuilder result = new StringBuilder();

      result.append(" ").append(this.getName());


      return result.substring(1);
   }

   public void publish(ModelCommand cmd) { 
      String yaml = Yaml.encode(cmd);
      activeCommands.put(cmd.getId(), cmd);
      oldCommands.add(cmd);
      send();
   }

   public void send() { 
      String yaml = Yaml.encode(activeCommands.values());
      for (String targetUrl : targetUrlList) {
         try {
            URL url = new URL(targetUrl);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setDoOutput(true);
            DataOutputStream out = new DataOutputStream(con.getOutputStream());
            out.writeBytes(yaml);
            out.flush();

            InputStream inputStream = con.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader in = new BufferedReader(inputStreamReader);
            String inputLine;
            StringBuffer content = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
               content.append(inputLine);
            }
            in.close();
            out.close();
            con.disconnect();

            // got an answer, clear active commands
            activeCommands.clear();
            LinkedHashMap<String, Object> map = Yaml.forPackage(service.getClass().getPackage().getName())
                  .decode(content.toString());
            executeCommands(map.values());

         }
         catch (Exception e) {
            e.printStackTrace();
         }
      }
   }

   public void executeCommands(Collection values) { 
      for (Object value : values) {
         try {
            ModelCommand cmd = (ModelCommand) value;
            this.service.getExecutor().submit(() -> cmd.run(this.service.getModelEditor()));
         }
         catch (Exception e) {
            e.printStackTrace();
         }
      }
   }

   public CommandStream start() { 
      service.getSpark().post("/" + name, (req, res) -> handlePostRequest(req, res));
      return this;
   }

   public String handlePostRequest(Request req, Response res) { 
      String body = req.body();
      LinkedHashMap<String, Object> commandMap = Yaml.forPackage(this.getClass().getPackage().getName())
            .decode(body);

      Collection values = commandMap.values();
      executeCommands(values);

      return "OK";
   }

   public void addCommandsToBeStreamed(String... commandList) { 
      for (String cmd : commandList) {
         service.getModelEditor().addCommandListener(cmd, this);
      }
   }

}
