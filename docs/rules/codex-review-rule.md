# Codex Review Rule

## 1. 문서 목적

Codex(공식 GitHub App, `chatgpt-codex-connector`)가 PR에 남기는 리뷰를 Claude Code가 자동으로 읽고 처리하기 위한 로컬 환경 설정 절차를 정의한다.

이 문서에는 토큰 값 자체를 포함하지 않는다. 토큰은 절대 커밋하지 않는다 (`CLAUDE.md` 절대 금지 7항).

---

## 2. 배경

- Codex의 상세 findings는 인라인 리뷰 코멘트로 남는데, 이건 `gh pr view`로는 읽을 수 없고 GitHub REST API(`GET /repos/{owner}/{repo}/pulls/{number}/comments`)로만 읽을 수 있다.
- `.claude/settings.json`의 `permissions.deny`에 `Bash(gh api *)`가 있어 `gh api`는 전체 차단되어 있다 (다른 deny 패턴을 우회할 수 있는 raw escape hatch라 의도적으로 막아둔 것 — 이 deny는 건드리지 않는다).
- 그래서 `gh api` 대신, **읽기 전용 권한만 가진 별도 토큰 + `curl`** 조합으로 이 저장소의 PR 리뷰 코멘트만 읽는다. `curl`은 `.claude/settings.json`에 이미 조건 없이 allow되어 있다.

---

## 3. 토큰 발급 절차 (GitHub UI, PC마다 반복)

1. GitHub 우측 상단 프로필 → **Settings**
2. 왼쪽 메뉴 **Developer settings**
3. **Personal access tokens** → **Fine-grained tokens** → **Generate new token**
4. 설정값:
   - **Token name**: `bid-radar-codex-review-read`
   - **Expiration**: 90일 권장 (만료 시 재발급)
   - **Resource owner**: `nini98`
   - **Repository access**: "Only select repositories" → `nini98/bid-radar`만 선택
   - **Permissions** → Repository permissions → **Pull requests: Read-only**, **Issues: Read-only** 두 개만 부여 (나머지는 No access)
     - `Issues: Read-only`가 없으면 5절의 `/issues/{PR번호}/reactions` 호출이 권한 부족으로 실패해, findings 없는 자동 리뷰(👍만 남는 경우)를 "자동 트리거 실패"로 오판하게 된다.
5. **Generate token** → 생성된 값은 그 페이지에서만 보이므로 즉시 복사

---

## 4. 로컬 환경변수 설정 (PC마다 반복)

토큰 값은 Claude에게 붙여넣지 않고, 터미널에서 직접 설정한다.

```bash
echo 'export BID_RADAR_GH_PR_READ_TOKEN="발급받은_토큰_값"' >> ~/.zshrc
source ~/.zshrc
```

환경변수 이름은 `BID_RADAR_GH_PR_READ_TOKEN`으로 고정한다 (다른 프로젝트 토큰과 혼동 방지).

---

## 5. 사용 방식

**Codex GitHub App의 push 자동 트리거는 간헐적으로만 작동한다** (2026-07-13 실측: PR#11에서 fix push 4건 중 2건만 자동 반응, 나머지 2건은 완전 침묵 — OpenAI 쪽의 알려진 버그, [openai/codex#15477](https://github.com/openai/codex/issues/15477) 참고). "PR을 열면 항상 자동 리뷰, 이후 push엔 항상 무반응"이 아니라 **push마다 결과가 다를 수 있다는 전제로 접근한다.**

Codex 앱은 이 저장소에 check run이나 commit status를 남기지 않는다(`gh pr checks`, `statusCheckRollup` 둘 다 항상 빈 값 — 2026-07-13 확인). 즉 "이번 push를 실제로 실행했는지"를 확정해주는 API 신호는 없고, 아래 세 엔드포인트로 간접 판단할 수밖에 없다.

전체 흐름:

1. Claude Code가 fix 커밋을 만들어 push한다. 이때 push 시각과 push 후 HEAD 커밋 SHA(`git rev-parse HEAD`)를 기록해둔다.
2. **세 엔드포인트를 모두 확인한다.** Codex는 findings가 있으면 인라인 리뷰 코멘트를, 실행 오류(트리거 실패·quota 등)가 있으면 PR 대화 코멘트를 남기고, **findings가 없으면 코멘트 없이 PR에 👍(`+1`) 리액션만 남긴다.**

```bash
# 공통: HTTP 상태코드까지 확인 (curl -s만 쓰면 4xx/5xx도 JSON 오류 본문을 그냥 출력해서
# "findings/코멘트 없음"과 "API 호출 자체가 실패함"을 구분 못 하게 된다)
fetch() {
  local url="$1"
  local resp status
  resp=$(curl -s -w '\n%{http_code}' -H "Authorization: Bearer $BID_RADAR_GH_PR_READ_TOKEN" \
    -H "Accept: application/vnd.github+json" "$url")
  status=$(tail -n1 <<< "$resp")
  if [ "$status" -ge 400 ]; then
    echo "API 오류 ($status): $url" >&2
    return 1
  fi
  sed '$d' <<< "$resp"
}

# 1) 인라인 리뷰 코멘트 (findings) — per_page 기본값 30이라 오래된 PR은 최신순 정렬 필수
fetch "https://api.github.com/repos/nini98/bid-radar/pulls/{PR번호}/comments?sort=created&direction=desc&per_page=100"

# 2) PR 대화 코멘트 (실행 오류 메시지 등, review comment와 다른 엔드포인트)
fetch "https://api.github.com/repos/nini98/bid-radar/issues/{PR번호}/comments?sort=created&direction=desc&per_page=100"

# 3) PR(issue) 리액션 (findings가 없을 때 👍만 남음)
fetch "https://api.github.com/repos/nini98/bid-radar/issues/{PR번호}/reactions?per_page=100"
```

`fetch`가 오류를 리턴하면(1번 curl이 실패하면) 그 자체를 "findings/코멘트 없음"으로 취급하지 않고 호출 자체를 실패로 중단한다.

세 엔드포인트는 판단 방식이 다르다.

- **인라인 리뷰 코멘트**: `user.login`이 `chatgpt-codex-connector[bot]`이면서 `commit_id`가 1번에서 기록한 push 후 HEAD SHA와 일치하는 것이 있으면 findings로 간주한다. `created_at`만으로는 지연 실행된 이전 커밋 대상 코멘트와 구분이 안 되므로, `commit_id` 일치를 기준으로 쓴다.
- **PR 대화 코멘트**: `user.login`이 봇이고 `created_at`이 push 시각 이후인 것이 있으면 내용을 읽고 실행 오류인지 확인한다 (대화 코멘트는 커밋에 안 묶이므로 `commit_id`가 없다 — `created_at`으로만 판단).
- **리액션은 "findings 없음"의 결정적 신호로 쓰지 않는다.** GitHub Reactions API는 같은 사용자가 같은 `content`(`+1`)를 다시 남기면 새 항목을 만들지 않고 기존 리액션을 그대로 반환하므로, 한 번이라도 Codex 봇의 `+1`이 남은 PR에서는 이후 push가 findings 없이 끝나도 `created_at`이 갱신되지 않는다. 리액션은 "이 PR이 과거에 최소 한 번은 리뷰된 적이 있다"는 참고 정보로만 쓴다.

3. 수 분(10분 이상) 기다려도 위 기준의 새 리뷰 코멘트/대화 코멘트가 없으면, **기존에 봇의 `+1` 리액션이 있었는지와 무관하게** `gh pr comment {PR번호} --body "@codex review"`로 재리뷰를 직접 트리거한다 (`gh pr comment`는 `.claude/settings.json`에서 막혀있지 않음). 트리거 후 다시 수 분 기다렸다가 위 2번 방식으로 재확인한다.
4. findings나 실행 오류가 있으면 처리 후 다시 1번으로 돌아간다. 재트리거 후에도 새 리뷰 코멘트/대화 코멘트가 끝내 없다면:
   - 이 PR에 봇의 `+1` 리액션이 **한 번도 없었다면** — 두 번의 시도(자동 + 수동)에도 응답이 전혀 없었다는 뜻이므로, 종료하지 말고 사용자에게 상황을 보고하고 GitHub PR 페이지에서 직접 확인해달라고 요청한다.
   - 이 PR에 봇의 `+1` 리액션이 **이미 있었다면** — "이번 push도 클린해서 조용한 것"과 "재트리거까지 했는데도 실행 자체가 안 된 것"을 API로는 구분할 수 없는 한계 상황이다. 자동으로 종료하지 않고, 이 사실을 사용자에게 알린 뒤 GitHub PR 페이지에서 최신 커밋 옆 리뷰 상태를 직접 확인해달라고 요청한다.

---

## 6. 절대 금지

- 토큰 값을 이 문서나 다른 어떤 파일에도 기록하지 않는다.
- 토큰 값을 커밋, PR 코멘트, 채팅 로그에 붙여넣지 않는다.
- `.claude/settings.json`의 `Bash(gh api *)` deny를 이 목적으로 풀지 않는다.
- 이 토큰에 Pull requests: Read-only, Issues: Read-only 이외의 권한을 추가하지 않는다.
