package javaPackagesToJavaDoc.JavaDoc;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyChangeListener;
import java.util.Objects;

public class JavaDocApp
{

   public static final String PROPERTY_content = "content";

   private Page content;

   protected PropertyChangeSupport listeners;
   public static final String PROPERTY_modelEditor = "modelEditor";
   private JavaDocEditor modelEditor;
   public static final String PROPERTY_id = "id";
   private String id;
   public static final String PROPERTY_description = "description";
   private String description;

   public Page getContent()
   {
      return this.content;
   }

   public JavaDocApp setContent(Page value)
   {
      if (this.content == value)
      {
         return this;
      }

      final Page oldValue = this.content;
      if (this.content != null)
      {
         this.content = null;
         oldValue.setApp(null);
      }
      this.content = value;
      if (value != null)
      {
         value.setApp(this);
      }
      this.firePropertyChange(PROPERTY_content, oldValue, value);
      return this;
   }

   public boolean firePropertyChange(String propertyName, Object oldValue, Object newValue)
   {
      if (this.listeners != null)
      {
         this.listeners.firePropertyChange(propertyName, oldValue, newValue);
         return true;
      }
      return false;
   }

   public boolean addPropertyChangeListener(PropertyChangeListener listener)
   {
      if (this.listeners == null)
      {
         this.listeners = new PropertyChangeSupport(this);
      }
      this.listeners.addPropertyChangeListener(listener);
      return true;
   }

   public boolean addPropertyChangeListener(String propertyName, PropertyChangeListener listener)
   {
      if (this.listeners == null)
      {
         this.listeners = new PropertyChangeSupport(this);
      }
      this.listeners.addPropertyChangeListener(propertyName, listener);
      return true;
   }

   public boolean removePropertyChangeListener(PropertyChangeListener listener)
   {
      if (this.listeners != null)
      {
         this.listeners.removePropertyChangeListener(listener);
      }
      return true;
   }

   public boolean removePropertyChangeListener(String propertyName, PropertyChangeListener listener)
   {
      if (this.listeners != null)
      {
         this.listeners.removePropertyChangeListener(propertyName, listener);
      }
      return true;
   }

   @Override
   public String toString()
   {
      final StringBuilder result = new StringBuilder();
      result.append(' ').append(this.getId());
      result.append(' ').append(this.getDescription());
      return result.substring(1);
   }

   public void removeYou()
   {
      this.setContent(null);
   }

   public JavaDocApp init(JavaDocEditor editor)
   {
      this.modelEditor = editor;
      this.setId("root");
      this.setDescription("JavaDoc App");
      return this;
   }

   public JavaDocEditor getModelEditor()
   {
      return this.modelEditor;
   }

   public JavaDocApp setModelEditor(JavaDocEditor value)
   {
      if (Objects.equals(value, this.modelEditor))
      {
         return this;
      }

      final JavaDocEditor oldValue = this.modelEditor;
      this.modelEditor = value;
      this.firePropertyChange(PROPERTY_modelEditor, oldValue, value);
      return this;
   }

   public String getId()
   {
      return this.id;
   }

   public JavaDocApp setId(String value)
   {
      if (Objects.equals(value, this.id))
      {
         return this;
      }

      final String oldValue = this.id;
      this.id = value;
      this.firePropertyChange(PROPERTY_id, oldValue, value);
      return this;
   }

   public String getDescription()
   {
      return this.description;
   }

   public JavaDocApp setDescription(String value)
   {
      if (Objects.equals(value, this.description))
      {
         return this;
      }

      final String oldValue = this.description;
      this.description = value;
      this.firePropertyChange(PROPERTY_description, oldValue, value);
      return this;
   }

}