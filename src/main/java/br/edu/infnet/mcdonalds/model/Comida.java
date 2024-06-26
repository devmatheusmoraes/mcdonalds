package br.edu.infnet.mcdonalds.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.*;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Getter
@Setter
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Comida extends Produto{

    @Column(nullable = true)
    private float peso;

    @Column(nullable = true)
    private boolean vegano;

    @Column(nullable = true)
    private String ingredientes;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    private static final Logger logger = LoggerFactory.getLogger(Comida.class);

    public Comida(String nome, float valor, UUID codigo, float peso, boolean vegano, String ingredientes){
        this.setNome(nome);
        this.setValor(valor);
        this.setCodigo(codigo);
        this.ingredientes = ingredientes;
        this.peso = peso;
        this.vegano = vegano;
        setPeso(peso);

    }

    @Override
    public void preparar() {
        logger.info("Preparando a Comida: {}", this.getNome());
    }
}
