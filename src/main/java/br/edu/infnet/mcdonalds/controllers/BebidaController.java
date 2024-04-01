package br.edu.infnet.mcdonalds.controllers;

import br.edu.infnet.mcdonalds.model.Bebida;
import br.edu.infnet.mcdonalds.model.Usuario;
import br.edu.infnet.mcdonalds.services.impl.BebidaServiceImpl;
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
@RequestMapping("/bebidas")
public class BebidaController {

    @Autowired
    BebidaServiceImpl bebidaService;

    @Autowired
    private HttpServletRequest request;

    private static final Logger logger = LoggerFactory.getLogger(BebidaController.class);

    @GetMapping("/listar-todas")
    public String getAll(Model model , @ModelAttribute("sucesso") Object sucesso,
                         @ModelAttribute("sucessoDelete") Object sucessoDelete,
                         @ModelAttribute("message") Object message) {
        model.addAttribute("module", "bebidas");
        model.addAttribute("bebidas", bebidaService.findAll());

        model.addAttribute("sucesso", sucesso);
        model.addAttribute("sucessoDelete", sucessoDelete);
        model.addAttribute("message", message);

        return "bebidas/index";
    }

    @GetMapping("/adicionarForm")
    public String showAdicionarForm(Bebida bebida, Model model){
        model.addAttribute("module", "bebidas");
        model.addAttribute("bebida", bebida);

        return "bebidas/add";
    }

    @PostMapping("/adicionar")
    public RedirectView add(Bebida bebida, Model model, RedirectAttributes redirectAttributes){
        logger.info(bebida.toString());
        Usuario usuarioLogado = (Usuario) request.getSession().getAttribute("usuarioLogado");
        bebida.setUsuario(usuarioLogado);
        bebidaService.save(bebida);
        redirectAttributes.addFlashAttribute("sucesso", true);
        redirectAttributes.addFlashAttribute("message", "Bebida adicionada com sucesso!");

        return new RedirectView("/bebidas/listar-todas");
    }

    @GetMapping("/delete/{codigo}")
    public RedirectView delete(@PathVariable("codigo") UUID codigo, Model model, RedirectAttributes redirectAttributes){

        model.addAttribute("module", "bebidas");

        try{
            bebidaService.deleteById(codigo);
            redirectAttributes.addFlashAttribute("sucesso", true);
            redirectAttributes.addFlashAttribute("message", "Bebida deletada com sucesso!");
        }catch (Exception ex){
            redirectAttributes.addFlashAttribute("sucessoDelete",false);
        }
        return new RedirectView("/bebidas/listar-todas");
    }

    @GetMapping("/update/{codigo}")
    public String showUpdateForm(@PathVariable("codigo") UUID codigo, Model model) {
        Optional<Bebida> bebidaOptional = bebidaService.findById(codigo);
        if (bebidaOptional.isPresent()) {
            Bebida bebida = bebidaOptional.get();
            model.addAttribute("bebida", bebida);
            return "bebidas/update";
        } else {
            return "redirect:/bebidas/listar-todas";
        }
    }

    @PostMapping("/update/{codigo}")
    public String update(@PathVariable("codigo") UUID codigo, Bebida bebida, Model model, RedirectAttributes redirectAttributes) {
        Optional<Bebida> bebidaOptional = bebidaService.findById(codigo);
        if (bebidaOptional.isPresent()) {
            Bebida bebidaExistente = bebidaOptional.get();
            bebidaExistente.setNome(bebida.getNome());
            bebidaExistente.setMarca(bebida.getMarca());
            bebidaExistente.setValor(bebida.getValor());
            bebidaExistente.setTamanho(bebida.getTamanho());
            bebidaService.save(bebidaExistente);
            redirectAttributes.addFlashAttribute("sucesso", true);
            redirectAttributes.addFlashAttribute("message", "Bebida editada com sucesso!");
        } else {
            redirectAttributes.addFlashAttribute("sucesso", false);
            redirectAttributes.addFlashAttribute("message", "Bebida não encontrada!");
        }
        return "redirect:/bebidas/listar-todas";
    }


}
