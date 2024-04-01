package br.edu.infnet.mcdonalds.controllers;

import br.edu.infnet.mcdonalds.model.Comida;
import br.edu.infnet.mcdonalds.model.Usuario;
import br.edu.infnet.mcdonalds.services.impl.ComidaServiceImpl;
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
@RequestMapping("/comidas")
public class ComidaController {

    @Autowired
    ComidaServiceImpl comidaService;

    @Autowired
    private HttpServletRequest request;

    private static final Logger logger = LoggerFactory.getLogger(ComidaController.class);

    @GetMapping("/listar-todas")
    public String getAll(Model model , @ModelAttribute("sucesso") Object sucesso,
                         @ModelAttribute("sucessoDelete") Object sucessoDelete,
                         @ModelAttribute("message") Object message) {
        model.addAttribute("module", "comidas");
        model.addAttribute("comidas", comidaService.findAll());

        model.addAttribute("sucesso", sucesso);
        model.addAttribute("sucessoDelete", sucessoDelete);
        model.addAttribute("message", message);

        return "comidas/index";
    }

    @GetMapping("/adicionarForm")
    public String showAdicionarForm(Comida comida, Model model){
        model.addAttribute("module", "comidas");
        model.addAttribute("comida", comida);

        return "comidas/add";
    }

    @PostMapping("/adicionar")
    public RedirectView add(Comida comida, Model model, RedirectAttributes redirectAttributes){
        logger.info(comida.toString());
        Usuario usuarioLogado = (Usuario) request.getSession().getAttribute("usuarioLogado");
        comida.setUsuario(usuarioLogado);
        comidaService.save(comida);
        redirectAttributes.addFlashAttribute("sucesso", true);
        redirectAttributes.addFlashAttribute("message", "Comida adicionada com sucesso!");

        return new RedirectView("/comidas/listar-todas");
    }

    @GetMapping("/delete/{codigo}")
    public RedirectView delete(@PathVariable("codigo") UUID codigo, Model model, RedirectAttributes redirectAttributes){

        model.addAttribute("module", "comidas");

        try{
            comidaService.deleteById(codigo);
            redirectAttributes.addFlashAttribute("sucesso", true);
            redirectAttributes.addFlashAttribute("message", "Comida deletada com sucesso!");
        }catch (Exception ex){
            redirectAttributes.addFlashAttribute("sucessoDelete",false);
        }
        return new RedirectView("/comidas/listar-todas");
    }

    @GetMapping("/update/{codigo}")
    public String showUpdateForm(@PathVariable("codigo") UUID codigo, Model model) {
        Optional<Comida> comidaOptional = comidaService.findById(codigo);
        if (comidaOptional.isPresent()) {
            Comida comida = comidaOptional.get();
            model.addAttribute("comida", comida);
            return "comidas/update";
        } else {
            return "redirect:/comidas/listar-todas";
        }
    }

    @PostMapping("/update/{codigo}")
    public String update(@PathVariable("codigo") UUID codigo, Comida comida, Model model, RedirectAttributes redirectAttributes) {
        Optional<Comida> comidaOptional = comidaService.findById(codigo);
        if (comidaOptional.isPresent()) {
            Comida comidaExistente = comidaOptional.get();
            comidaExistente.setNome(comida.getNome());
            comidaExistente.setPeso(comida.getPeso());
            comidaExistente.setValor(comida.getValor());
            comidaExistente.setIngredientes(comida.getIngredientes());
            comidaService.save(comidaExistente);
            redirectAttributes.addFlashAttribute("sucesso", true);
            redirectAttributes.addFlashAttribute("message", "Comida editada com sucesso!");
        } else {
            redirectAttributes.addFlashAttribute("sucesso", false);
            redirectAttributes.addFlashAttribute("message", "Comida não encontrada!");
        }
        return "redirect:/comidas/listar-todas";
    }
}
