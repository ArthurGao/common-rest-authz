package com.arthur.rest.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;

@SuppressWarnings("rawtypes")
public class EnumToStringConverterFactory implements ConverterFactory<String, Enum> {

  @SuppressWarnings("unchecked")
  public <T extends Enum> Converter<String, T> getConverter(Class<T> targetType) {
    return new EnumToString(targetType);
  }

  private final class EnumToString<T extends Enum> implements Converter<String, T> {

    private Class<T> enumType;

    public EnumToString(Class<T> enumType) {
      this.enumType = enumType;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T convert(String source) {
      return (T) Enum.valueOf(this.enumType, source.trim().toUpperCase());
    }
  }
}