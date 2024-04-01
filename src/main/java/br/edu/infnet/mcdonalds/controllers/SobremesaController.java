package br.edu.infnet.mcdonalds.controllers;

import br.edu.infnet.mcdonalds.model.Sobremesa;
import br.edu.infnet.mcdonalds.model.Usuario;
import br.edu.infnet.mcdonalds.services.impl.SobremesaServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import java.util.Optional;
import java.util.UUID;

@Controller
@RequestMapping("/sobremesas")
public class SobremesaController {

    @Autowired
    SobremesaServiceImpl sobremesaService;

    @Autowired
    private HttpServletRequest request;

    private static final Logger logger = LoggerFactory.getLogger(SobremesaController.class);

    @GetMapping("/listar-todas")
    public String getAll(Model model , @ModelAttribute("sucesso") Object sucesso,
                         @ModelAttribute("sucessoDelete") Object sucessoDelete,
                         @ModelAttribute("message") Object message) {
        model.addAttribute("module", "sobremesas");
        model.addAttribute("sobremesas", sobremesaService.findAll());

        model.addAttribute("sucesso", sucesso);
        model.addAttribute("sucessoDelete", sucessoDelete);
        model.addAttribute("message", message);

        return "sobremesas/index";
    }

    @GetMapping("/adicionarForm")
    public String showAdicionarForm(Sobremesa sobremesa, Model model){
        model.addAttribute("module", "sobremesas");
        model.addAttribute("sobremesa", sobremesa);

        return "sobremesas/add";
    }

    @PostMapping("/adicionar")
    public RedirectView add(Sobremesa sobremesa, Model model, RedirectAttributes redirectAttributes){
        logger.info(sobremesa.toString());
        Usuario usuarioLogado = (Usuario) request.getSession().getAttribute("usuarioLogado");
        sobremesa.setUsuario(usuarioLogado);
        sobremesaService.save(sobremesa);
        redirectAttributes.addFlashAttribute("sucesso", true);
        redirectAttributes.addFlashAttribute("message", "Sobremesa adicionada com sucesso!");

        return new RedirectView("/sobremesas/listar-todas");
    }

    @GetMapping("/delete/{codigo}")
    public RedirectView delete(@PathVariable("codigo") UUID codigo, Model model, RedirectAttributes redirectAttributes){

        model.addAttribute("module", "sobremesas");

        try{
            sobremesaService.deleteById(codigo);
            redirectAttributes.addFlashAttribute("sucesso", true);
            redirectAttributes.addFlashAttribute("message", "Sobremesa deletada com sucesso!");
        }catch (Exception ex){
            redirectAttributes.addFlashAttribute("sucessoDelete",false);
        }
        return new RedirectView("/sobremesas/listar-todas");
    }

    @GetMapping("/update/{codigo}")
    public String showUpdateForm(@PathVariable("codigo") UUID codigo, Model model) {
        Optional<Sobremesa> sobremesaOptional = sobremesaService.findById(codigo);
        if (sobremesaOptional.isPresent()) {
            Sobremesa sobremesa = sobremesaOptional.get();
            model.addAttribute("sobremesa", sobremesa);
            return "sobremesas/update";
        } else {
            return "redirect:/sobremesas/listar-todas";
        }
    }

    @PostMapping("/update/{codigo}")
    public String update(@PathVariable("codigo") UUID codigo, Sobremesa sobremesa, Model model, RedirectAttributes redirectAttributes) {
        Optional<Sobremesa> sobremesaOptional = sobremesaService.findById(codigo);
        if (sobremesaOptional.isPresent()) {
            Sobremesa sobremesaExistente = sobremesaOptional.get();
            sobremesaExistente.setNome(sobremesa.getNome());
            sobremesaExistente.setInformacao(sobremesa.getInformacao());
            sobremesaExistente.setValor(sobremesa.getValor());
            sobremesaExistente.setQuantidade(sobremesa.getQuantidade());
            sobremesaService.save(sobremesaExistente);
            redirectAttributes.addFlashAttribute("sucesso", true);
            redirectAttributes.addFlashAttribute("message", "Sobremesa editada com sucesso!");
        } else {
            redirectAttributes.addFlashAttribute("sucesso", false);
            redirectAttributes.addFlashAttribute("message", "Sobremesa não encontrada!");
        }
        return "redirect:/sobremesas/listar-todas";
    }
}
