package organizacao.finance.Guaxicash.service;

import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import organizacao.finance.Guaxicash.Config.SecurityService;
import organizacao.finance.Guaxicash.entities.Enums.UserRole;
import organizacao.finance.Guaxicash.entities.Missions;
import organizacao.finance.Guaxicash.entities.User;
import organizacao.finance.Guaxicash.repositories.MissionsRepository;
import organizacao.finance.Guaxicash.service.exceptions.ResourceNotFoundExeption;

import java.util.List;
import java.util.UUID;

@Service
public class MissionsService {

    private final MissionsRepository missionsRepository;
    private SecurityService securityService;

    public MissionsService(MissionsRepository missionsRepository,
                           SecurityService securityService) {
        this.missionsRepository = missionsRepository;
        this.securityService = securityService;
    }

    private boolean isAdmin(User u){ return u.getRole() == UserRole.ADMIN; }

    /** Admin vê tudo; usuário vê apenas o que ainda não concluiu. */
    public List<Missions> listForCurrentUser() {
        User me = securityService.obterUserLogin();
        if (isAdmin(me)) return missionsRepository.findAll();
        return missionsRepository.findAllNotCompletedByUser(me.getUuid());
    }

    /** Útil para endpoints que precisem explicitamente “todas”. */
    public List<Missions> listAll() {
        return missionsRepository.findAll();
    }

    /** Caso precise listar disponíveis para um usuário específico (admin). */
    public List<Missions> listAvailableFor(UUID userId) {
        return missionsRepository.findAllNotCompletedByUser(userId);
    }

    public Missions get(UUID id) {
        return missionsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundExeption("Missão não encontrada"));
    }

    @Transactional
    public Missions create(Missions body) {
        // ignora UUID vindo no body (se vier)
        body.setUuid(null);
        return missionsRepository.save(body);
    }

    @Transactional
    public Missions update(UUID id, Missions body) {
        Missions m = missionsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundExeption("Missão não encontrada"));
        // atualiza apenas campos editáveis
        m.setTitle(body.getTitle());
        m.setDescription(body.getDescription());
        m.setValue(body.getValue());
        return m; // JPA dirty checking salva no commit
    }

    @Transactional
    public void delete(UUID id) {
        if (!missionsRepository.existsById(id)) {
            throw new ResourceNotFoundExeption("Missão não encontrada");
        }
        missionsRepository.deleteById(id);
    }
}
