package unikassel.qsexample.Ramp;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyChangeListener;

public class RampApp  
{

   public static final String PROPERTY_modelEditor = "modelEditor";

   private RampEditor modelEditor;

   public RampEditor getModelEditor()
   {
      return modelEditor;
   }

   public RampApp setModelEditor(RampEditor value)
   {
      if (value != this.modelEditor)
      {
         RampEditor oldValue = this.modelEditor;
         this.modelEditor = value;
         firePropertyChange("modelEditor", oldValue, value);
      }
      return this;
   }

   public static final String PROPERTY_id = "id";

   private String id;

   public String getId()
   {
      return id;
   }

   public RampApp setId(String value)
   {
      if (value == null ? this.id != null : ! value.equals(this.id))
      {
         String oldValue = this.id;
         this.id = value;
         firePropertyChange("id", oldValue, value);
      }
      return this;
   }

   public static final String PROPERTY_description = "description";

   private String description;

   public String getDescription()
   {
      return description;
   }

   public RampApp setDescription(String value)
   {
      if (value == null ? this.description != null : ! value.equals(this.description))
      {
         String oldValue = this.description;
         this.description = value;
         firePropertyChange("description", oldValue, value);
      }
      return this;
   }

   public static final String PROPERTY_content = "content";

   private Page content = null;

   public Page getContent()
   {
      return this.content;
   }

   public RampApp setContent(Page value)
   {
      if (this.content != value)
      {
         Page oldValue = this.content;
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
         firePropertyChange("content", oldValue, value);
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
      result.append(" ").append(this.getDescription());
      result.append(" ").append(this.getSupplyId());


      return result.substring(1);
   }

   public void removeYou()
   {
      this.setContent(null);

   }

   private String toolbar = "button supply | button palettes";

   public void supply()
   {
      Page page = new Page().setId("supplyPage").setDescription(toolbar).setApp(this);

      for (RampSupply supply : modelEditor.getRampSupplys().values()) {
         new Line().setId(supply.getId())
               .setDescription(String.format("button %s %s %s", supply.getSupplier().getId(), supply.getProduct().getDescription(), supply.getState()))
               .setPage(page)
               .setAction(String.format("OpenAddPalette?supply=%s addPalette", supply.getId()));
      }
   }

   public void addPalette() {
      Page page = new Page().setId("addPalettePage").setDescription(toolbar).setApp(this);
      new Line().setId("supplyLine").setPage(page)
            .setDescription(String.format("supply %s", supplyId));
      new Line().setId("paletteIn").setPage(page).setDescription("input palette id?");
      new Line().setId("itemsIn").setPage(page).setDescription("input items?");
      new Line().setId("addButton").setPage(page).setDescription("button add");
   }

   public static final String PROPERTY_supplyId = "supplyId";

   private String supplyId;

   public String getSupplyId()
   {
      return supplyId;
   }

   public RampApp setSupplyId(String value)
   {
      if (value == null ? this.supplyId != null : ! value.equals(this.supplyId))
      {
         String oldValue = this.supplyId;
         this.supplyId = value;
         firePropertyChange("supplyId", oldValue, value);
      }
      return this;
   }

   public RampApp init(RampEditor editor) // no fulib
   {
      this.modelEditor = editor;
      this.setId("root");
      this.setDescription("Ramp App");
      supply();
      return this;
   }

}
