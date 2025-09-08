package abdaty_technologie.API_Invest.Entity;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

@MappedSuperclass
@Getter
@Setter
public abstract class BaseEntity {
  @Id @GeneratedValue(strategy = GenerationType.UUID)
  private String id;

  @Column(name = "created_at", nullable=false, updatable=false) 
  private Instant creation;

  @Column(name = "updated_at", nullable=false) 
  private Instant modification;
}
