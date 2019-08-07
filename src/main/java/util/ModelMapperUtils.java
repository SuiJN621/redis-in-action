package util;

import java.lang.reflect.Type;

import org.modelmapper.ModelMapper;

public class ModelMapperUtils {

    private static ModelMapper modelMapper = new ModelMapper() {
        {
            getConfiguration().setAmbiguityIgnored(Boolean.TRUE);
        }
    };

    public static <T> T map(Object source, Class<T> destinationType) {
        return modelMapper.map(source, destinationType);
    }

    public static <T> T map(Object source, Type destinationType) {
        return modelMapper.map(source, destinationType);
    }

    public static ModelMapper getModelMapper() {
        return modelMapper;
    }
}