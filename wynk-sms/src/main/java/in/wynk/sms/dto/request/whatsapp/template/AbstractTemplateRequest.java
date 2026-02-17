package in.wynk.sms.dto.request.whatsapp.template;

import com.github.annotation.analytic.core.annotations.AnalysedEntity;
import in.wynk.sms.dto.request.whatsapp.IWhatsappRequest;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@AnalysedEntity
@NoArgsConstructor
public abstract class AbstractTemplateRequest implements IWhatsappRequest {
}
