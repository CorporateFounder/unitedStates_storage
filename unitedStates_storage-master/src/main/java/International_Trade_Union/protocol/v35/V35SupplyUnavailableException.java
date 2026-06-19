package International_Trade_Union.protocol.v35;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
public class V35SupplyUnavailableException extends RuntimeException {
    public V35SupplyUnavailableException(String message) {
        super(message);
    }

    public V35SupplyUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
