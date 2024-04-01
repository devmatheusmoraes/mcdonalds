package br.edu.infnet.mcdonalds.controllers;

import br.edu.infnet.mcdonalds.model.Role;
import br.edu.infnet.mcdonalds.model.Usuario;
import br.edu.infnet.mcdonalds.services.impl.UsuarioServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Controller
@RequestMapping("/usuarios")
public class UsuarioController {

    @Autowired
    UsuarioServiceImpl usuarioService;

    private static final Logger logger = LoggerFactory.getLogger(UsuarioController.class);

    @GetMapping("/listar-todas")
    public String getAll(Model model , @ModelAttribute("sucesso") Object sucesso,
                         @ModelAttribute("sucessoDelete") Object sucessoDelete,
                         @ModelAttribute("message") Object message) {
        model.addAttribute("module", "usuarios");
        model.addAttribute("usuarios", usuarioService.findAll());

        model.addAttribute("sucesso", sucesso);
        model.addAttribute("sucessoDelete", sucessoDelete);
        model.addAttribute("message", message);

        return "usuarios/index";
    }

    @GetMapping("/adicionarForm")
    public String showAdicionarForm(Usuario usuario, Model model){
        model.addAttribute("module", "usuarios");
        model.addAttribute("usuario", usuario);

        return "usuarios/add";
    }

    @PostMapping("/adicionar")
    public RedirectView add(Usuario usuario, Model model, RedirectAttributes redirectAttributes){
        logger.info(usuario.toString());
        usuarioService.save(usuario);
        redirectAttributes.addFlashAttribute("sucesso", true);
        redirectAttributes.addFlashAttribute("message", "Usuario adicionado com sucesso!");

        return new RedirectView("/usuarios/listar-todas");
    }

    @GetMapping("/delete/{id}")
    public RedirectView delete(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes){

        model.addAttribute("module", "usuarios");

        try{
            usuarioService.deleteById(id);
            redirectAttributes.addFlashAttribute("sucesso", true);
            redirectAttributes.addFlashAttribute("message", "Usuario deletado com sucesso!");
        }catch (Exception ex){
            redirectAttributes.addFlashAttribute("sucessoDelete",false);
        }
        return new RedirectView("/usuarios/listar-todas");
    }

    @GetMapping("/update/{id}")
    public String showUpdateForm(@PathVariable("id") Long id, Model model) {
        Optional<Usuario> usuarioOptional = usuarioService.findById(id);
        if (usuarioOptional.isPresent()) {
            Usuario usuario = usuarioOptional.get();
            model.addAttribute("usuario", usuario);
            model.addAttribute("roles", usuario.getRoles());
            return "usuarios/update";
        } else {
            return "redirect:/usuarios/listar-todas";
        }
    }

    @PostMapping("/update/{id}")
    public String update(@PathVariable("id") Long id, Usuario usuario, Model model, RedirectAttributes redirectAttributes) {
        Optional<Usuario> usuarioOptional = usuarioService.findById(id);
        if (usuarioOptional.isPresent()) {
            Usuario usuarioExistente = usuarioOptional.get();
            usuarioExistente.setNome(usuario.getNome());
            usuarioExistente.setEmail(usuario.getEmail());
            usuarioExistente.setPassword(usuario.getPassword());
            usuarioExistente.setStatus(usuario.getStatus());
            usuarioExistente.setRoles(usuario.getRoles());
            usuarioExistente.setPedidos(usuario.getPedidos());
            usuarioService.save(usuarioExistente);
            redirectAttributes.addFlashAttribute("sucesso", true);
            redirectAttributes.addFlashAttribute("message", "Usuario editado com sucesso!");
        } else {
            redirectAttributes.addFlashAttribute("sucesso", false);
            redirectAttributes.addFlashAttribute("message", "Usuario não encontrado!");
        }
        return "redirect:/usuarios/listar-todas";
    }
}
