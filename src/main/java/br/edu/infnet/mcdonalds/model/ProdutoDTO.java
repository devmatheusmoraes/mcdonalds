package br.edu.infnet.mcdonalds.model;

import lombok.*;

import java.util.UUID;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProdutoDTO {

    private UUID codigo;
    private String nome;
    private float valor;
}
