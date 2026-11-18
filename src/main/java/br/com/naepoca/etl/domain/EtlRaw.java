package br.com.naepoca.etl.domain;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode; import org.hibernate.type.SqlTypes;
import java.time.Instant; import java.util.Map;
@Entity @Table(name="etl_raw")
public class EtlRaw {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
  @Column(name="etl_run_id", nullable=false) private Long etlRunId;
  @Column(nullable=false) private String fonte;
  @JdbcTypeCode(SqlTypes.JSON) @Column(nullable=false, columnDefinition="CLOB") private Map<String,Object> payload;
  @Column(name="coletado_em") private Instant coletadoEm = Instant.now();
  public Long getId(){return id;} public Long getEtlRunId(){return etlRunId;} public void setEtlRunId(Long v){etlRunId=v;}
  public String getFonte(){return fonte;} public void setFonte(String v){fonte=v;}
  public Map<String,Object> getPayload(){return payload;} public void setPayload(Map<String,Object> v){payload=v;}
}
