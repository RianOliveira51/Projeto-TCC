package organizacao.finance.Guaxicash.service;

import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import organizacao.finance.Guaxicash.entities.Missions;
import organizacao.finance.Guaxicash.repositories.MissionsRepository;
import organizacao.finance.Guaxicash.service.exceptions.ResourceNotFoundExeption;

import java.util.List;
import java.util.UUID;

@Service
public class MissionsService {

    private final MissionsRepository missionsRepository;
    public MissionsService(MissionsRepository missionsRepository) {
        this.missionsRepository = missionsRepository;
    }

    // padrão, sem paginação
    public List<Missions> listAll() {
        return missionsRepository.findAll();
        // se quiser já ordenado por título:
        // return missionsRepository.findAll(Sort.by("title").ascending());
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
