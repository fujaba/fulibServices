package javaPackagesToJavaDoc.JavaPackages;

import java.util.Objects;

public class HaveSubUnit extends ModelCommand
{
   @Override
   public Object run(JavaPackagesEditor editor)
   {
      JavaPackage obj = (JavaPackage) editor.getOrCreate(JavaPackage.class, getId());
      JavaPackage parent = (JavaPackage) editor.getObjectFrame(JavaPackage.class, this.parent);
      obj.setUp(parent);

      return obj;
   }

   @Override
   public void undo(JavaPackagesEditor editor)
   {
      JavaPackage obj = (JavaPackage) editor.removeModelObject(getId());
      obj.setUp(null);
   }

   @Override
   public ModelCommand parse(Object currentObject)
   {
      if ( ! (currentObject instanceof JavaPackage)) {
         return null;
      }

      JavaPackage currentPackage = (JavaPackage) currentObject;

      if (currentPackage.getUp() == null) {
         return null;
      }

      ModelCommand modelCommand = new HaveSubUnit().setParent(currentPackage.getUp().getId()).setId(currentPackage.getId());

      return modelCommand;
   }

   @Override
   public String toString()
   {
      final StringBuilder result = new StringBuilder(super.toString());
      result.append(' ').append(this.getParent());
      return result.toString();
   }

   public String getParent()
   {
      return this.parent;
   }

   public HaveSubUnit setParent(String value)
   {
      if (Objects.equals(value, this.parent))
      {
         return this;
      }

      final String oldValue = this.parent;
      this.parent = value;
      this.firePropertyChange(PROPERTY_parent, oldValue, value);
      return this;
   }
   public static final String PROPERTY_parent = "parent";
   private String parent;

}
