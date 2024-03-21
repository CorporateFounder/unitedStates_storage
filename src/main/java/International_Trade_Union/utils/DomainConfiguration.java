package International_Trade_Union.utils;

import lombok.Data;
import org.springframework.core.env.Environment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Data
public class DomainConfiguration {
    public DomainConfiguration() {
    }

    public DomainConfiguration(String port, String host, String publlc_domain) {
        this.port = port;
        this.host = host;
        this.publlc_domain = publlc_domain;
    }

    @Value("${server.port}")
    private String port;

    @Value("${server.address}")
    private String host;

    @Value("${public_domain}")
    private String publlc_domain;

    @Override
    public String toString() {
        return "DomainConfiguration{" +
                "port='" + port + '\'' +
                ", host='" + host + '\'' +
                ", publlc_domain='" + publlc_domain + '\'' +
                '}';
    }
}
