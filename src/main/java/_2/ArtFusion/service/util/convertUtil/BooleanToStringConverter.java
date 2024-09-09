package _2.ArtFusion.service.util.convertUtil;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class BooleanToStringConverter implements AttributeConverter<Boolean, String> {

    @Override
    public String convertToDatabaseColumn(Boolean attribute) {
        if (attribute != null) {
            return attribute ? "Y" : "N"; // true는 "Y", false는 "N"으로 저장
        }
        return null;
    }

    @Override
    public Boolean convertToEntityAttribute(String dbData) {
        if ("Y".equals(dbData)) {
            return true;
        } else if ("N".equals(dbData)) {
            return false;
        }
        return null;
    }
}
