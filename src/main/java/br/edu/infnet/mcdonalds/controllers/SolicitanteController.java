package br.edu.infnet.mcdonalds.controllers;

import br.edu.infnet.mcdonalds.model.Usuario;
import br.edu.infnet.mcdonalds.services.impl.SolicitanteServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import br.edu.infnet.mcdonalds.model.Solicitante;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import java.util.Optional;

@Controller
@RequestMapping("/solicitantes")
public class SolicitanteController {

    @Autowired
    SolicitanteServiceImpl solicitanteService;

    @Autowired
    private HttpServletRequest request;

    private static final Logger logger = LoggerFactory.getLogger(SolicitanteController.class);

    @GetMapping("/listar-todas")
    public String getAll(Model model , @ModelAttribute("sucesso") Object sucesso,
                         @ModelAttribute("sucessoDelete") Object sucessoDelete,
                         @ModelAttribute("message") Object message) {
        model.addAttribute("module", "solicitantes");
        model.addAttribute("solicitantes", solicitanteService.findAll());

        model.addAttribute("sucesso", sucesso);
        model.addAttribute("sucessoDelete", sucessoDelete);
        model.addAttribute("message", message);

        return "solicitantes/index";
    }

    @GetMapping("/adicionarForm")
    public String showAdicionarForm(Solicitante solicitante, Model model){
        model.addAttribute("module", "solicitantes");
        model.addAttribute("solicitante", solicitante);

        return "solicitantes/add";
    }

    @PostMapping("/adicionar")
    public RedirectView add(Solicitante solicitante, Model model, RedirectAttributes redirectAttributes){
        logger.info(solicitante.toString());

        Usuario usuarioLogado = (Usuario) request.getSession().getAttribute("usuarioLogado");
        solicitante.setUsuario(usuarioLogado);
        solicitanteService.save(solicitante);
        redirectAttributes.addFlashAttribute("sucesso", true);
        redirectAttributes.addFlashAttribute("message", "Solicitante adicionada com sucesso!");

        return new RedirectView("/solicitantes/listar-todas");
    }

    @GetMapping("/delete/{codigo}")
    public RedirectView delete(@PathVariable("codigo") Long codigo, Model model, RedirectAttributes redirectAttributes){

        model.addAttribute("module", "solicitantes");

        try{
            solicitanteService.deleteById(codigo);
            redirectAttributes.addFlashAttribute("sucesso", true);
            redirectAttributes.addFlashAttribute("message", "Solicitante deletada com sucesso!");
        }catch (Exception ex){
            redirectAttributes.addFlashAttribute("sucessoDelete",false);
        }
        return new RedirectView("/solicitantes/listar-todas");
    }

    @GetMapping("/update/{codigo}")
    public String showUpdateForm(@PathVariable("codigo") Long codigo, Model model) {
        Optional<Solicitante> solicitanteOptional = solicitanteService.findById(codigo);
        if (solicitanteOptional.isPresent()) {
            Solicitante solicitante = solicitanteOptional.get();
            model.addAttribute("solicitante", solicitante);
            return "solicitantes/update";
        } else {
            return "redirect:/solicitantes/listar-todas";
        }
    }

    @PostMapping("/update/{codigo}")
    public String update(@PathVariable("codigo") Long codigo, Solicitante solicitante, Model model, RedirectAttributes redirectAttributes) throws Exception {
        Optional<Solicitante> solicitanteOptional = solicitanteService.findById(codigo);
        if (solicitanteOptional.isPresent()) {
            Solicitante solicitanteExistente = solicitanteOptional.get();
            solicitanteExistente.setNome(solicitante.getNome());
            solicitanteExistente.setEmail(solicitante.getEmail());
            solicitanteExistente.setCpf(solicitante.getCpf());
            solicitanteExistente.setListaDePedido(solicitante.getListaDePedido());
            solicitanteService.save(solicitanteExistente);
            redirectAttributes.addFlashAttribute("sucesso", true);
            redirectAttributes.addFlashAttribute("message", "Solicitante editada com sucesso!");
        } else {
            redirectAttributes.addFlashAttribute("sucesso", false);
            redirectAttributes.addFlashAttribute("message", "Solicitante não encontrada!");
        }
        return "redirect:/solicitantes/listar-todas";
    }
}
