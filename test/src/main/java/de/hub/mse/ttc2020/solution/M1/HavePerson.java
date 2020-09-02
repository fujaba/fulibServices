package de.hub.mse.ttc2020.solution.M1;

import java.util.Objects;

public class HavePerson extends ModelCommand
{
   @Override
   public Object run(M1Editor editor)
   {
      Person person = (Person) editor.getOrCreate(Person.class, getId());
      person.setName(name).setAge(age);
      return person;
   }

   @Override
   public ModelCommand parse(Object currentObject)
   {
      if (currentObject instanceof Person) {
         // yes, its me
         Person currentPerson = (Person) currentObject;
         HavePerson modelCommand = new HavePerson();
         modelCommand.setId(currentPerson.getId());
         modelCommand.setName(currentPerson.getName())
               .setAge(currentPerson.getAge());
         return modelCommand;
      }
      return null;
   }

   @Override
   public String toString()
   {
      final StringBuilder result = new StringBuilder(super.toString());
      result.append(' ').append(this.getName());
      return result.toString();
   }

   public String getName()
   {
      return this.name;
   }

   public HavePerson setName(String value)
   {
      if (Objects.equals(value, this.name))
      {
         return this;
      }

      final String oldValue = this.name;
      this.name = value;
      this.firePropertyChange(PROPERTY_name, oldValue, value);
      return this;
   }

   public int getAge()
   {
      return this.age;
   }

   public HavePerson setAge(int value)
   {
      if (value == this.age)
      {
         return this;
      }

      final int oldValue = this.age;
      this.age = value;
      this.firePropertyChange(PROPERTY_age, oldValue, value);
      return this;
   }
   public static final String PROPERTY_name = "name";
   private String name;
   public static final String PROPERTY_age = "age";
   private int age;

}
