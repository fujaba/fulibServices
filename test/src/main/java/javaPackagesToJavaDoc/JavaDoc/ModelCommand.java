package javaPackagesToJavaDoc.JavaDoc;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyChangeListener;
import org.fulib.yaml.Reflector;
import org.fulib.yaml.StrUtil;
import java.lang.reflect.Method;
import org.fulib.tables.ObjectTable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.*;

public class ModelCommand  
{

   public static final String PROPERTY_id = "id";

   private String id;

   public String getId()
   {
      return id;
   }

   public ModelCommand setId(String value)
   {
      if (value == null ? this.id != null : ! value.equals(this.id))
      {
         String oldValue = this.id;
         this.id = value;
         firePropertyChange("id", oldValue, value);
      }
      return this;
   }

   public static final String PROPERTY_time = "time";

   private String time;

   public String getTime()
   {
      return time;
   }

   public ModelCommand setTime(String value)
   {
      if (value == null ? this.time != null : ! value.equals(this.time))
      {
         String oldValue = this.time;
         this.time = value;
         firePropertyChange("time", oldValue, value);
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

   @Override
   public String toString()
   {
      StringBuilder result = new StringBuilder();

      result.append(" ").append(this.getId());
      result.append(" ").append(this.getTime());


      return result.substring(1);
   }

   public ModelCommand parse(Object currentObject) { 
      Pattern pattern = havePattern();

      if (pattern == null) {
         return null;
      }

      PatternObject firstPatternObject = pattern.getObjects().get(0);
      if ( ! firstPatternObject.getHandleObjectClass().equals(currentObject.getClass())) {
         // not my business
         return null;
      }

      ObjectTable objectTable = new ObjectTable(firstPatternObject.getPoId(), currentObject);
      LinkedHashMap<PatternObject, ObjectTable> mapPatternObject2Table = new LinkedHashMap<>();
      mapPatternObject2Table.put(firstPatternObject, objectTable);

      matchAttributesAndLinks(pattern, mapPatternObject2Table, firstPatternObject, objectTable);

      // retrieve command
      ArrayList rows = new ArrayList();
      objectTable.filterRows( m -> { rows.add(m); return true; });
      if (rows.size() == 0) {
         return null;
      }

      Map<String, Object> firstRow = (Map<String, Object>) rows.get(0);
      ModelCommand newCommand = null;
      try {
         newCommand = this.getClass().getConstructor().newInstance();
         Reflector commandReflector = new Reflector().setClazz(newCommand.getClass());
         for (PatternObject patternObject : pattern.getObjects()) {
            String poId = patternObject.getPoId();
            for (PatternAttribute attribute : patternObject.getAttributes()) {
               String commandParamName = attribute.getCommandParamName();
               Object value = firstRow.get(poId + "." + attribute.getHandleAttrName());
               commandReflector.setValue(newCommand, commandParamName, "" + value);
            }
         }
      }
      catch (Exception e) {
         e.printStackTrace();
      }

      return newCommand;
   }

   public Object run(JavaDocEditor editor) { 
      Pattern pattern = havePattern();

      if (pattern == null) {
         return null;
      }

      // have handle objects
      for (PatternObject patternObject : pattern.getObjects()) {
         String handleObjectId = (String) getHandleObjectAttributeValue(patternObject, "id");
         if (handleObjectId == null) {
            // do not handle
            continue;
         }
         Class handleObjectClass = patternObject.getHandleObjectClass();
         Object handleObject = null;
         if (patternObject.getKind().equals("core") ) {
            handleObject = editor.getOrCreate(handleObjectClass, handleObjectId);
         }
         else if (patternObject.getKind().equals("context")) {
            handleObject = editor.getObjectFrame(handleObjectClass, handleObjectId);
         }
         else { // nac
            handleObject = editor.getObjectFrame(handleObjectClass, handleObjectId);
            editor.removeModelObject(handleObjectId);
         }

         patternObject.setHandleObject(handleObject);
      }


      for (PatternObject patternObject : pattern.getObjects()) {
         if (patternObject.getHandleObject() == null) {
            continue;
         }

         Reflector commandReflector = new Reflector().setClassName(this.getClass().getName());
         Reflector handleObjectReflector = new Reflector().setClassName(patternObject.getHandleObject().getClass().getName());

         for (PatternAttribute patternAttribute : patternObject.getAttributes()) {
            if ("id".equals(patternAttribute.getHandleAttrName())) {
               continue;
            }

            Object paramValue = commandReflector.getValue(this, patternAttribute.getCommandParamName());
            paramValue = paramValue.toString();
            handleObjectReflector.setValue(patternObject.getHandleObject(), patternAttribute.getHandleAttrName(), paramValue, null);
         }

         Object sourceHandleObject = patternObject.getHandleObject();
         for (PatternLink patternLink : patternObject.getLinks()) {
            String linkName = patternLink.getHandleLinkName();
            Object targetHandleObject = patternLink.getTarget().getHandleObject();
            if (patternLink.getKind().equals("core")) {
               handleObjectReflector.setValue(sourceHandleObject, linkName, targetHandleObject, null);
            }
            else if (patternLink.getKind().equals("nac")) {
               // remove link
               if (targetHandleObject != null) {
                  try {
                     Method withoutMethod = sourceHandleObject.getClass().getMethod("without" + StrUtil.cap(linkName), new Object[0].getClass());
                     if (withoutMethod != null) {
                        withoutMethod.invoke(sourceHandleObject, new Object[] {new Object[] {targetHandleObject}});
                     }
                     else {
                        Method setMethod = sourceHandleObject.getClass().getMethod("set" + StrUtil.cap(linkName), patternLink.getTarget().getHandleObjectClass());
                        setMethod.invoke(sourceHandleObject, new Object[] {null});
                     }
                  }
                  catch (Exception e) {
                     e.printStackTrace();
                  }
               }
               else {
                  try {
                     Method setMethod = sourceHandleObject.getClass().getMethod("set" + StrUtil.cap(linkName), patternLink.getTarget().getHandleObjectClass());
                     setMethod.invoke(sourceHandleObject, new Object[] {null});
                  }
                  catch (Exception e) {
                     e.printStackTrace();
                  }
               }
            }
         }
      }
      return null;
   }

   public void undo(JavaDocEditor editor) { 
      Pattern pattern = havePattern();

      if (pattern == null) {
         return;
      }

      for (PatternObject patternObject : pattern.getObjects()) {
         if (patternObject.getKind() == "core") {
            String id = (String) getHandleObjectAttributeValue(patternObject, "id");
            Object handleObject = editor.getObjectFrame(null, id);
            for (PatternLink link : patternObject.getLinks()) {
               String linkName = link.getHandleLinkName();
               Reflector handleObjectReflector = new Reflector().setClassName(handleObject.getClass().getName());
               Object value = handleObjectReflector.getValue(handleObject, linkName);
               if (value != null && value instanceof java.util.Collection) {
                  try {
                     java.lang.reflect.Method withoutMethod = handleObject.getClass().getMethod("without" + linkName.substring(0, 1).toUpperCase() + linkName.substring(1), new Object[]{}.getClass());
                     withoutMethod.invoke(handleObject, value);
                  }
                  catch (Exception e) {
                     e.printStackTrace();
                  }
               }
               else {
                  try {
                     java.lang.reflect.Method setMethod = handleObject.getClass().getMethod("set" + linkName.substring(0, 1).toUpperCase() + linkName.substring(1),
                           link.getTarget().getHandleObjectClass());
                     setMethod.invoke(handleObject, new Object[]{null});
                  }
                  catch (Exception e) {
                     e.printStackTrace();
                  }
               }
            }
         }
      }
   }

   public void matchAttributesAndLinks(Pattern pattern, LinkedHashMap mapPatternObject2Table, PatternObject currentPatternObject, ObjectTable objectTable) { 
      // match attributes
      String poId = currentPatternObject.getPoId();
      for (PatternAttribute attribute : currentPatternObject.getAttributes()) {
         String attrName = attribute.getHandleAttrName();
         objectTable.expandAttribute(poId + "." +attrName, attrName);
      }

      // match links
      for (PatternLink link : currentPatternObject.getLinks()) {
         PatternObject target = link.getTarget();
         ObjectTable targetTable = (ObjectTable) mapPatternObject2Table.get(target);

         if (link.getKind().equals("core")) {
            if (targetTable != null) {
               objectTable.hasLink(link.getHandleLinkName(), targetTable);
            }
            else {
               targetTable = objectTable.expandLink(target.getPoId(), link.getHandleLinkName());
               mapPatternObject2Table.put(target, targetTable);
               matchAttributesAndLinks(pattern, mapPatternObject2Table, target, targetTable);
            }
         }
         else if (link.getKind().equals("nac")) {
            if (targetTable != null) {
               ObjectTable myTable = targetTable;
               objectTable.filterRows(
                     map -> getSetOfTargetHandles((Map<String, Object>) map, poId, link.getHandleLinkName())
                           .contains(((Map<String, Object>) map).get(myTable.getColumnName())));
            }
            else {
               objectTable.filterRows(map -> getSetOfTargetHandles((Map<String, Object>) map, poId, link.getHandleLinkName()).isEmpty());
            }
         }
      }
   }

   public Set getSetOfTargetHandles(Map map, String poId, String linkName) { 
      Object sourceHandleObject = map.get(poId);
      ObjectTable tableFocusedOnSource = new ObjectTable(poId, sourceHandleObject);
      ObjectTable tableFocusedOnLinkTarget = tableFocusedOnSource.expandLink(linkName, linkName);

      return tableFocusedOnLinkTarget.toSet();

   }

   public Pattern havePattern() { 
      return null;
   }

   public Object getHandleObjectAttributeValue(PatternObject patternObject, String handleAttributeName) { 
      Reflector reflector = new Reflector().setClassName(this.getClass().getName());
      for (PatternAttribute patternAttribute : patternObject.getAttributes()) {
         if (patternAttribute.getHandleAttrName().equals(handleAttributeName)) {
            String commandParamName = patternAttribute.getCommandParamName();
            Object value = reflector.getValue(this, commandParamName);
            return value;
         }
      }
      return null;
   }

}
