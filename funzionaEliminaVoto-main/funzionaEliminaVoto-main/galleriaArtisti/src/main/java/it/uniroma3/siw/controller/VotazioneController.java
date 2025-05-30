package it.uniroma3.siw.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import it.uniroma3.siw.Comparator.ComparatoreOpere;
import it.uniroma3.siw.model.Artista;
import it.uniroma3.siw.model.Opera;
import it.uniroma3.siw.model.Votazione;
import it.uniroma3.siw.repository.OperaRepository;
import it.uniroma3.siw.service.ArtistaService;
import it.uniroma3.siw.service.CredenzialiService;
import it.uniroma3.siw.service.OperaService;
import it.uniroma3.siw.service.VotazioneService;

@Controller
public class VotazioneController {
	@Autowired
    private OperaService operaService;

    @Autowired
    private VotazioneService votazioneService;
    @Autowired
    private CredenzialiService credenzialiService;
    @Autowired
    private OperaRepository operaRepository;
    @Autowired
    private ArtistaService artistaService;

    @RequestMapping("/votazione")
    public String votazione(@RequestParam(value = "username") String username, @RequestParam(value = "titolo", required = false) String titolo, Model model) {
        List<Opera> opere = new ArrayList<>(operaService.findAll());
        opere.sort(new ComparatoreOpere());
        model.addAttribute("username", username);
        if (titolo != null) {
            List<Opera> listaNuova = new ArrayList<>();
            for (Opera opera : opere) {
                if (opera.getTitolo().equals(titolo)) {
                    listaNuova.add(opera);
                }
            }
            if (listaNuova.isEmpty()) {
                model.addAttribute("errore", "Nessun opera trovato con questo titolo!");
                return "votazione";
            }
            model.addAttribute("opere", listaNuova);
            return "votazione";
        }
        model.addAttribute("opere", opere);
        return "votazione";
    }

    @GetMapping("/votaOpera")
    public String votaOpera(@RequestParam("username") String username, @RequestParam("operaId") Long operaId, Model model, RedirectAttributes redirectAttributes) {
    	Opera opera = operaService.findById(operaId);
        List<Opera> opere = operaService.findAll();
        if (username.equals(opera.getArtista().getCredenziali().getUsername())) {
            model.addAttribute("stessoUtente", "Non puoi votare un opera di cui sei l'autore!");
            model.addAttribute("opere", opere);
            model.addAttribute("username", username);
            return "votazione";
        }

        model.addAttribute("opera", opera);
        model.addAttribute("username", username);
        redirectAttributes.addAttribute("votato", "Il tuo voto è stato salvato con successo!");
        return "votaOpera";
    }

    @PostMapping("/inviaVoto")
    public String inviaVoto(@RequestParam(value = "username") String username, @RequestParam("voto") int voto, @RequestParam("operaId") Long operaId, Model model) {
        List<Votazione> votazioni = votazioneService.findAll();
        Long utenteId = credenzialiService.findIdByUsername(username);
        for (Votazione votazione : votazioni) {
            if (Objects.equals(votazione.getMittenteId(), utenteId) && votazione.getOpera().equals(operaService.findById(operaId))) {
                model.addAttribute("votoPassato", "Hai già votato questa opera! Elimina il voto per eseguirne un altro!");
                List<Opera> opere = operaService.findAll();
                opere.sort(new ComparatoreOpere());
                model.addAttribute("username", username);
                model.addAttribute("opere", opere);
                return "votazione";
            }
        }
        Artista artista = operaService.findById(operaId).getArtista();
        Votazione votazione = new Votazione();
        votazione.setMittenteId(utenteId);
        votazione.setDestinatario(artista);
        votazione.setOpera(operaService.findById(operaId));
        votazione.setVoto(voto);
        votazioneService.save(votazione);
        int votoArtista = artista.getVotoTotale();
        int nuovoVotoArtista = votoArtista + voto;
        artista.setVotoTotale(nuovoVotoArtista);
        artistaService.save(artista);
        Opera opera = operaService.findById(operaId);
        int nuovoVoto = opera.getVoto() + voto;
        opera.setVoto(nuovoVoto);
        operaService.save(opera);
        List<Opera> opere = operaService.findAll();
        opere.sort(new ComparatoreOpere());
        model.addAttribute("username", username);
        model.addAttribute("opere", opere);
        model.addAttribute("votoInviato", "Voto salvato con successo!");
        return "votazione";
    }
    
    @GetMapping("/eliminaVoto")
    public String eliminaVoto(@RequestParam("username") String username, @RequestParam("operaId") Long operaId, Model model) {
        model.addAttribute("username", username);
        Opera opera = operaService.findById(operaId);
        List<Votazione> votazioni = votazioneService.findAll();
        Long utenteId = credenzialiService.findIdByUsername(username); 
     // Qui stampi l'utenteId per verificare che sia corretto
        System.out.println("utenteId: " + utenteId);
        for (Votazione votazione : votazioni) {
            if (Objects.equals(votazione.getMittenteId(), utenteId) && Objects.equals(votazione.getOpera(), operaService.findById(operaId))) {
                int votoSingolo = votazione.getVoto();
                int votoNuovo = opera.getVoto() - votoSingolo;
                opera.setVoto(votoNuovo);
                operaService.save(opera);
                Artista artista = opera.getArtista();
                int votoArtista = artista.getVotoTotale();
                int nuovoVotoArtista = votoArtista - votazione.getVoto();
                artista.setVotoTotale(nuovoVotoArtista);
                artistaService.save(artista);
                votazioneService.delete(votazione);
                model.addAttribute("votoEliminato", "Il voto è stato eliminato con successo!");
                List<Opera> opere = operaService.findAll();
                opere.sort(new ComparatoreOpere());
                model.addAttribute("opere", opere);
                return "votazione";
            }
        }
        model.addAttribute("nonVotato", "Non hai votato quest'opera!");
        List<Opera> opere = operaService.findAll();
        opere.sort(new ComparatoreOpere());
        model.addAttribute("opere", opere);
        return "votazione";
    }

    
}
