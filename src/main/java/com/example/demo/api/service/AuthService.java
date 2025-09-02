// 登録サービス/コントローラ
@Service
@RequiredArgsConstructor
public class AuthService {
  private final UserRepository users;
  private final PasswordEncoder encoder;

  @Transactional
  public void register(String email, String rawPassword, String username) {
    User u = new User();
    u.setEmail(email);
    u.setUsername(username);
    u.setPassword(encoder.encode(rawPassword)); // ← ここが重要
    users.save(u);
  }
}
