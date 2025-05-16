package vn.tnteco.common.core.context;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import vn.tnteco.common.core.converter.DataConverter;

@Component
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ConverterContext {

    @Setter
    @Getter
    private static DataConverter dataConverter;

    @Autowired
    public ConverterContext(@Qualifier("dataConverter") DataConverter dataConverter) {
        setDataConverter(dataConverter);
    }

}
