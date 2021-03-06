package unikassel.websystem.Shop;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyChangeListener;

public class AddToCart extends ModelCommand  
{
   @Override
   public Object run(ShopEditor modelEditor)
   {
      // have an order
      ShopOrder shoppingCard = _app.getShoppingCart();
      if (shoppingCard == null) {
         String orderId = "order_" + (modelEditor.getShopOrders().size() + 1);
         String customerId = null;
         if (_app.getCustomer() != null) {
            customerId = _app.getCustomer().getId();
         }
         shoppingCard = new HaveOrderCommand()
               .setId(orderId)
               .setState("collecting-items")
               .setCustomer(customerId)
               .run(modelEditor);
         _app.setShoppingCart(shoppingCard);
      }

      // add position
      ShopOrderPosition pos = new HaveOrderPositionCommand().setId(String.format("%s_pos_%d", shoppingCard.getId(), shoppingCard.getPositions().size() + 1))
            .setOrder(shoppingCard.getId())
            .setAmount(1.0)
            .setOffer(this.getOffer())
            .run(modelEditor);
      return pos;
   }

   public static final String PROPERTY_offer = "offer";

   private String offer;

   public String getOffer()
   {
      return offer;
   }

   public AddToCart setOffer(String value)
   {
      if (value == null ? this.offer != null : ! value.equals(this.offer))
      {
         String oldValue = this.offer;
         this.offer = value;
         firePropertyChange("offer", oldValue, value);
      }
      return this;
   }

   public static final String PROPERTY__app = "_app";

   private ShopApp _app;

   public ShopApp get_app()
   {
      return _app;
   }

   public AddToCart set_app(ShopApp value)
   {
      if (value != this._app)
      {
         ShopApp oldValue = this._app;
         this._app = value;
         firePropertyChange("_app", oldValue, value);
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

      result.append(" ").append(this.getOffer());


      return result.substring(1);
   }

   public boolean preCheck(ShopEditor editor) { 
      if (this.getTime() == null) {
         this.setTime(editor.getTime());
      }
      RemoveCommand oldRemove = editor.getRemoveCommands().get("AddToCart-" + this.getId());
      if (oldRemove != null) {
         return false;
      }
      ModelCommand oldCommand = editor.getActiveCommands().get("AddToCart-" + this.getId());
      if (oldCommand != null && java.util.Objects.compare(oldCommand.getTime(), this.getTime(), (a,b) -> a.compareTo(b)) >= 0) {
         return false;
      }
      editor.getActiveCommands().put("AddToCart-" + this.getId(), this);
      return true;
   }

}
