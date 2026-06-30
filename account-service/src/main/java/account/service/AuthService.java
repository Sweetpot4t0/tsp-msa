package account.service;

import account.entity.Member;
import account.exception.DuplicateUsernameException;
import account.repository.MemberRepository;
import account.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public void register(String username, String rawPassword) {
        if (memberRepository.findByUsername(username).isPresent()) {
            throw new DuplicateUsernameException(username);
        }
        memberRepository.save(new Member(username, passwordEncoder.encode(rawPassword), "USER"));
    }

    public String login(String username, String rawPassword) {
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new BadCredentialsException("Invalid username or password"));
        if (!passwordEncoder.matches(rawPassword, member.getPassword())) {
            throw new BadCredentialsException("Invalid username or password");
        }
        return jwtTokenProvider.createToken(member.getUsername(), member.getRole());
    }
}
