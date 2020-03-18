package unikassel.websystem.Shop;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyChangeListener;
import static spark.Spark.*;

import org.fulib.scenarios.MockupTools;
import org.fulib.yaml.ReflectorMap;
import spark.Request;
import spark.Response;

import java.util.LinkedHashMap;
import org.json.JSONObject;
import org.fulib.yaml.Reflector;

import java.lang.reflect.Method;
import java.util.concurrent.ExecutorService;

public class ShopService  
{
   public static void main(String[] args)
   {
      new ShopService().setMyPort(5050).start();
   }

   public ShopService() {
      String streams = System.getenv("streams");
      if (streams != null) {
         String[] split = streams.split("\n");
         for (String oneStream : split) {
            String[] words = oneStream.split(" ");
            streamUrls.put(words[0], words[1]);
         }
      }
   }

   private LinkedHashMap<String, String> streamUrls = new LinkedHashMap<>();

   public ShopService addStreamUrl(String streamName, String targetUrl) {
      streamUrls.put(streamName, targetUrl);
      return this;
   }

   public static final String PROPERTY_myPort = "myPort";

   private int myPort;

   public int getMyPort()
   {
      return myPort;
   }

   public ShopService setMyPort(int value)
   {
      if (value != this.myPort)
      {
         int oldValue = this.myPort;
         this.myPort = value;
         firePropertyChange("myPort", oldValue, value);
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

   public static final String PROPERTY_modelEditor = "modelEditor";

   private ShopEditor modelEditor;

   public ShopEditor getModelEditor()
   {
      return modelEditor;
   }

   public ShopService setModelEditor(ShopEditor value)
   {
      if (value != this.modelEditor)
      {
         ShopEditor oldValue = this.modelEditor;
         this.modelEditor = value;
         firePropertyChange("modelEditor", oldValue, value);
      }
      return this;
   }

   public static final String PROPERTY_currentSession = "currentSession";

   private String currentSession;

   public String getCurrentSession()
   {
      return currentSession;
   }

   public ShopService setCurrentSession(String value)
   {
      if (value == null ? this.currentSession != null : ! value.equals(this.currentSession))
      {
         String oldValue = this.currentSession;
         this.currentSession = value;
         firePropertyChange("currentSession", oldValue, value);
      }
      return this;
   }

   public static final String PROPERTY_sessionToAppMap = "sessionToAppMap";

   private LinkedHashMap<String, ShopApp> sessionToAppMap = new LinkedHashMap();

   public LinkedHashMap<String, ShopApp> getSessionToAppMap()
   {
      return sessionToAppMap;
   }

   public ShopService setSessionToAppMap(LinkedHashMap<String, ShopApp> value)
   {
      if (value != this.sessionToAppMap)
      {
         LinkedHashMap<String, ShopApp> oldValue = this.sessionToAppMap;
         this.sessionToAppMap = value;
         firePropertyChange("sessionToAppMap", oldValue, value);
      }
      return this;
   }

   public static final String PROPERTY_reflectorMap = "reflectorMap";

   private ReflectorMap reflectorMap;

   public ReflectorMap getReflectorMap()
   {
      return reflectorMap;
   }

   public ShopService setReflectorMap(ReflectorMap value)
   {
      if (value != this.reflectorMap)
      {
         ReflectorMap oldValue = this.reflectorMap;
         this.reflectorMap = value;
         firePropertyChange("reflectorMap", oldValue, value);
      }
      return this;
   }

   public static final String PROPERTY_executor = "executor";

   private ExecutorService  executor;

   public ExecutorService  getExecutor()
   {
      return executor;
   }

   public ShopService setExecutor(ExecutorService  value)
   {
      if (value != this.executor)
      {
         ExecutorService  oldValue = this.executor;
         this.executor = value;
         firePropertyChange("executor", oldValue, value);
      }
      return this;
   }

   @Override
   public String toString()
   {
      StringBuilder result = new StringBuilder();

      result.append(" ").append(this.getCurrentSession());


      return result.substring(1);
   }

   public void start() // no fulib
   {
      if (myPort <= 0) {
         myPort = 4571;
      }
      String envPort = System.getenv("PORT");
      if (envPort != null) {
         myPort = Integer.parseInt(envPort);
      }
      executor = java.util.concurrent.Executors.newSingleThreadExecutor();
      modelEditor = new ShopEditor();
      reflectorMap = new ReflectorMap(this.getClass().getPackage().getName());

      try { port(myPort);} catch (Exception e) {};
      get("/", (req, res) -> executor.submit( () -> this.getFirstRoot(req, res)).get());
      get("/shop", (req, res) -> executor.submit( () -> this.getFirstRoot(req, res)).get());
      post("/cmd", (req, res) -> executor.submit( () -> this.cmd(req, res)).get());
      post("/shopcmd", (req, res) -> executor.submit( () -> this.cmd(req, res)).get());

      CommandStream stream = new CommandStream();
      modelEditor.addCommandListener(HaveOrderCommand.class.getSimpleName(), stream);
      stream.start("http://localhost:5050/StoreToShop", this);

      notFound((req, resp) -> {
         return "404 not found: " + req.requestMethod() + req.url() + req.body();
      });

      java.util.logging.Logger.getGlobal().info("Shop Service is listening on port " + myPort);
   }

   public String getFirstRoot(Request req, Response res) { 
      currentSession = "" + (sessionToAppMap.size() + 1);
      return root(req, res);
   }

   public String root(Request req, Response res) { 
      try
      {
         ShopApp myApp = this.sessionToAppMap.get(currentSession);
         if (myApp == null) {
            myApp = new ShopApp().init(this.modelEditor);
            sessionToAppMap.put(currentSession, myApp);
         }

         java.util.Map<String, String> params = req.params();
         java.io.StringWriter stringWriter = new java.io.StringWriter();
         MockupTools.htmlTool().dumpScreen(stringWriter, myApp);
         StringBuilder page = new StringBuilder(stringWriter.toString());
         int paramPos = page.indexOf("_cmd: words[0],");
         String sessionParam = String.format("_session: '%s', ", currentSession);
         page.insert(paramPos, sessionParam);
         int cmdUrlPos = page.indexOf("'/cmd'");
         page.insert(cmdUrlPos + 2, "shop");
         String sessionPage = page.toString();
         return sessionPage;
      }
      catch (Exception e)
      {
         e.printStackTrace();
         return "404 " + e.getMessage();
      }
   }

   public String cmd(Request req, Response res) { 
      String cmd = req.body();
      JSONObject jsonObject = new JSONObject(cmd);

      this.currentSession = jsonObject.getString("_session");

      ShopApp app = sessionToAppMap.get(currentSession);

      if (app == null) {
         return "404 could not find session " + currentSession;
      }

      if (jsonObject.keySet().size() > 3) {
         String cmdClassName = jsonObject.getString("_cmd");
         Reflector reflector = reflectorMap.getReflector(cmdClassName);
         Object cmdObject = reflector.newInstance();
         for (String key : jsonObject.keySet()) {
            if (key.startsWith("_")) {
               continue;
            }
            else {
               // assign to command attribute
               String attrName = key;
               if (key.endsWith("In")) {
                  attrName = key.substring(0, key.length() - 2);
               }
               String value = jsonObject.getString(key);
               reflector.setValue(cmdObject, attrName, value, null);
            }
         }
         // call command
         try {
            Method runMethod = cmdObject.getClass().getMethod("run", ShopEditor.class);
            runMethod.invoke(cmdObject, modelEditor);

         }
         catch (Exception e) {
            e.printStackTrace();
            return "404 " + e.getMessage();
         }
      }

      // next page
      String newPage = jsonObject.getString("_newPage");
      try {
         Method method = app.getClass().getMethod(newPage, new Class[0]);
         method.invoke(app);
      }
      catch (Exception e) {
         return "404 app has no method to compute page " + newPage;
      }

      return root(req, res);
   }

}
