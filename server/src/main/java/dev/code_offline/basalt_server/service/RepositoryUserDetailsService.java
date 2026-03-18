package dev.code_offline.basalt_server.service;

import dev.code_offline.basalt_server.repository.PersonRepository;
import dev.code_offline.basalt_share.model.Person;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class RepositoryUserDetailsService implements UserDetailsService {
	private final PersonRepository personRepository;
	
	public RepositoryUserDetailsService(PersonRepository personRepository) {
		this.personRepository = personRepository;
	}
	
	@Override
	public @NonNull UserDetails loadUserByUsername(@NonNull String username) throws UsernameNotFoundException {
		Person person = personRepository.findByUsername(username);
		
		if (person == null) {
			throw new UsernameNotFoundException("User not found: " + username);
		}
		
		return person;
	}
}
