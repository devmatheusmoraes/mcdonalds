package br.edu.infnet.mcdonalds.controllers;

import br.edu.infnet.mcdonalds.model.*;
import br.edu.infnet.mcdonalds.model.Pedido;
import br.edu.infnet.mcdonalds.services.impl.*;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Controller
@RequestMapping("/pedidos")
public class PedidoController {

    @Autowired
    PedidoServiceImpl pedidoService;

    @Autowired
    SolicitanteServiceImpl solicitanteService;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private BebidaServiceImpl bebidaService;

    @Autowired
    private ComidaServiceImpl comidaService;

    @Autowired
    private SobremesaServiceImpl sobremesaService;

    private static final Logger logger = LoggerFactory.getLogger(PedidoController.class);

    @GetMapping("/listar-todas")
    public String getAll(Model model , @ModelAttribute("sucesso") Object sucesso,
                         @ModelAttribute("sucessoDelete") Object sucessoDelete,
                         @ModelAttribute("message") Object message) {
        model.addAttribute("module", "pedidos");
        model.addAttribute("pedidos", pedidoService.findAll());

        model.addAttribute("sucesso", sucesso);
        model.addAttribute("sucessoDelete", sucessoDelete);
        model.addAttribute("message", message);

        return "pedidos/index";
    }

    @GetMapping("/adicionarForm")
    public String showAdicionarForm(Pedido pedido, Model model){
        model.addAttribute("module", "pedidos");
        model.addAttribute("pedido", new Pedido());
        model.addAttribute("produtos", getAllProdutos());
        model.addAttribute("solicitantes", solicitanteService.findAll());

        return "pedidos/add";
    }

    private List<ProdutoDTO> getAllProdutos() {
        List<ProdutoDTO> produtos = new ArrayList<>();

        // Adiciona as bebidas
        bebidaService.findAll().forEach(bebida -> {
            ProdutoDTO produtoDTO = new ProdutoDTO();
            produtoDTO.setCodigo(bebida.getCodigo());
            produtoDTO.setNome(bebida.getNome());
            produtoDTO.setValor(bebida.getValor());
            produtos.add(produtoDTO);
        });

        // Adiciona as comidas
        comidaService.findAll().forEach(comida -> {
            ProdutoDTO produtoDTO = new ProdutoDTO();
            produtoDTO.setCodigo(comida.getCodigo());
            produtoDTO.setNome(comida.getNome());
            produtoDTO.setValor(comida.getValor());
            produtos.add(produtoDTO);
        });

        // Adiciona as sobremesas
        sobremesaService.findAll().forEach(sobremesa -> {
            ProdutoDTO produtoDTO = new ProdutoDTO();
            produtoDTO.setCodigo(sobremesa.getCodigo());
            produtoDTO.setNome(sobremesa.getNome());
            produtoDTO.setValor(sobremesa.getValor());
            produtos.add(produtoDTO);
        });

        return produtos;
    }

    @PostMapping("/adicionar")
    public RedirectView add(@ModelAttribute("pedido") Pedido pedido,
                      @RequestParam("listaDeProdutoUUIDs") List<UUID> listaDeProdutoUUIDs,
                      @RequestParam("pedido.solicitante.codigo") String codigoSolicitante,
                      Model model, RedirectAttributes redirectAttributes){
        logger.info(pedido.toString());
        pedido.setCodigo(UUID.randomUUID());

        Usuario usuarioLogado = (Usuario) request.getSession().getAttribute("usuarioLogado");

        List<PedidoProduto> produtosDoPedido = new ArrayList<>();

        for (UUID  idProduto : listaDeProdutoUUIDs) {
            Produto produto = null;

            if (comidaService.findById(idProduto).isPresent()) {
                produto = comidaService.findById(idProduto).get();
            } else if (bebidaService.findById(idProduto).isPresent()) {
                produto = bebidaService.findById(idProduto).get();
            } else if (sobremesaService.findById(idProduto).isPresent()) {
                produto = sobremesaService.findById(idProduto).get();
            }

            if (produto != null) {
                produtosDoPedido.add(new PedidoProduto(pedido, produto));
            }
        }

        Optional<Solicitante> optionalSolicitante = solicitanteService.findById(Long.valueOf(codigoSolicitante));

        optionalSolicitante.ifPresent(pedido::setSolicitante);

        pedido.setListaDeProduto(produtosDoPedido);
        pedido.setUsuario(usuarioLogado);
        pedidoService.save(pedido);
        redirectAttributes.addFlashAttribute("sucesso", true);
        redirectAttributes.addFlashAttribute("message", "Pedido adicionado com sucesso!");

        return new RedirectView("/pedidos/listar-todas");
    }

    @GetMapping("/delete/{codigo}")
    public RedirectView delete(@PathVariable("codigo") UUID codigo, Model model, RedirectAttributes redirectAttributes){

        model.addAttribute("module", "pedidos");

        try{
            pedidoService.deleteById(codigo);
            redirectAttributes.addFlashAttribute("sucesso", true);
            redirectAttributes.addFlashAttribute("message", "Pedido deletado com sucesso!");
        }catch (Exception ex){
            redirectAttributes.addFlashAttribute("sucessoDelete",false);
        }
        return new RedirectView("/pedidos/listar-todas");
    }

    @GetMapping("/update/{codigo}")
    public String showUpdateForm(@PathVariable("codigo") UUID codigo, Model model) {
        Optional<Pedido> pedidoOptional = pedidoService.findById(codigo);
        if (pedidoOptional.isPresent()) {
            Pedido pedido = pedidoOptional.get();
            model.addAttribute("pedido", pedido);
            return "pedidos/update";
        } else {
            return "redirect:/pedidos/listar-todas";
        }
    }

    @PostMapping("/update/{codigo}")
    public String update(@PathVariable("codigo") UUID codigo, Pedido pedidoAtualizado, Model model, RedirectAttributes redirectAttributes) {
        Optional<Pedido> pedidoOptional = pedidoService.findById(codigo);
        if (pedidoOptional.isPresent()) {
            Pedido pedidoExistente = pedidoOptional.get();
            pedidoExistente.setCodigo(pedidoAtualizado.getCodigo());
            pedidoExistente.setDescricao(pedidoAtualizado.getDescricao());
            pedidoExistente.setData(pedidoAtualizado.getData());
            pedidoExistente.getListaDeProduto().clear();
            for (PedidoProduto pedidoProduto : pedidoAtualizado.getListaDeProduto()) {
                pedidoProduto.setPedido(pedidoExistente);
                pedidoExistente.getListaDeProduto().add(pedidoProduto);
            }
            //pedidoExistente.setListaDeProduto(pedidoAtualizado.getListaDeProduto());
            pedidoService.save(pedidoExistente);
            redirectAttributes.addFlashAttribute("sucesso", true);
            redirectAttributes.addFlashAttribute("message", "Pedido editado com sucesso!");
        } else {
            redirectAttributes.addFlashAttribute("sucesso", false);
            redirectAttributes.addFlashAttribute("message", "Pedido não encontrado!");
        }
        return "redirect:/pedidos/listar-todas";
    }
}
