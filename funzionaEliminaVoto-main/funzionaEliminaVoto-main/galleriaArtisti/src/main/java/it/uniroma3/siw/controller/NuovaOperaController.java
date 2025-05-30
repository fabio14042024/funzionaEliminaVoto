package it.uniroma3.siw.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import it.uniroma3.siw.model.Artista;
import it.uniroma3.siw.model.Opera;
import it.uniroma3.siw.service.ArtistaService;
import it.uniroma3.siw.service.OperaService;


@RequestMapping("/artista")
@Controller
public class NuovaOperaController {
	 @Autowired
	    private ArtistaService artistaService;
	    @Autowired
	    private OperaService operaService;

	    @GetMapping("/nuovaOpera")
	    public String nuovaOpera(@RequestParam("username") String username, Model model) {
	        Artista artista = artistaService.findByCredenzialiUsername(username);
	        model.addAttribute("artistaId", artista.getId());
	        return "nuovaOpera";
	    }

	    @PostMapping("/operaAggiunta")
	    public String operaAggiunta(@RequestParam("titolo") String titolo,
	                                @RequestParam("descrizione") String descrizione,
	                                @RequestParam("artistaId") Long artistaId,
	                                @RequestParam("correnteArtistica") String correnteArtistica,
	                                @RequestParam("annoCreazione") Integer annoCreazione,
	                                @RequestParam("urlImage") String urlImage,
	                                RedirectAttributes redirectAttributes) {
	        Opera opera = new Opera();
	        Artista artista = artistaService.findById(artistaId);
	        opera.setTitolo(titolo);
	        opera.setDescrizione(descrizione);
	        opera.setVoto(0);
	        opera.setArtista(artista);
	        opera.setCorrenteArtistica(correnteArtistica);
	        opera.setUrlImage(urlImage);
	        opera.setAnnoCreazione(annoCreazione);
	        artista.getOpere().add(opera);
	        operaService.save(opera);
	        redirectAttributes.addFlashAttribute("operaAggiunta", "Opera Aggiunta con successo!");
	        return "redirect:/";
	    }
}
