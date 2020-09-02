package javaPackagesToJavaDoc.JavaPackages;

import java.util.Objects;

public class AddStreamCommand extends ModelCommand
{
   public static final String PROPERTY_incommingRoute = "incommingRoute";
   private String incommingRoute;
   public static final String PROPERTY_outgoingUrl = "outgoingUrl";
   private String outgoingUrl;

   @Override
   public String toString()
   {
      final StringBuilder result = new StringBuilder(super.toString());
      result.append(' ').append(this.getIncommingRoute());
      result.append(' ').append(this.getOutgoingUrl());
      return result.toString();
   }

   @Override
   public Object run(JavaPackagesEditor editor)
   {
      CommandStream stream = editor.getService().getStream(incommingRoute);
      stream.getTargetUrlList().add(outgoingUrl);
      return null;
   }

   public String getIncommingRoute()
   {
      return this.incommingRoute;
   }

   public AddStreamCommand setIncommingRoute(String value)
   {
      if (Objects.equals(value, this.incommingRoute))
      {
         return this;
      }

      final String oldValue = this.incommingRoute;
      this.incommingRoute = value;
      this.firePropertyChange(PROPERTY_incommingRoute, oldValue, value);
      return this;
   }

   public String getOutgoingUrl()
   {
      return this.outgoingUrl;
   }

   public AddStreamCommand setOutgoingUrl(String value)
   {
      if (Objects.equals(value, this.outgoingUrl))
      {
         return this;
      }

      final String oldValue = this.outgoingUrl;
      this.outgoingUrl = value;
      this.firePropertyChange(PROPERTY_outgoingUrl, oldValue, value);
      return this;
   }

}
