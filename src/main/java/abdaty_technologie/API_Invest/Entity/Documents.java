package abdaty_technologie.API_Invest.Entity;

import java.sql.Blob;

import abdaty_technologie.API_Invest.Entity.Enum.TypeDocuments;
import abdaty_technologie.API_Invest.Entity.Enum.TypePieces;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Documents extends BaseEntity {

    @Column(name="type_piece", nullable = false, length = 50)
    @Enumerated(EnumType.STRING) 
    private TypePieces typePiece;

    @Column(name="type_document", nullable = false, length = 50)
    @Enumerated(EnumType.STRING) 
    private TypeDocuments typeDocument;

    @Column(name="num_piece", nullable = false, length = 50)
    private String numPiece;

    @Column(name="photo_piece", nullable = false)
    private Blob photoPiece;

    @ManyToOne(optional = false)
    @JoinColumn(name = "personne_id")
    private Persons personne;
    
    @ManyToOne(optional = false)
    @JoinColumn(name = "entreprise_id")
    private Entreprise entreprise;
}

