package _2.ArtFusion.service.util.convertUtil;

import org.jetbrains.annotations.NotNull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class IntegerToBooleanConverter implements Converter<Integer, Boolean> {

    @Override
    public Boolean convert(@NotNull Integer source) {
        return source != null && source == 1;
    }
}