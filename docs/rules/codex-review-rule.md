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
   - **Permissions** → Repository permissions → **Pull requests: Read-only**만 부여 (나머지는 No access)
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

전체 흐름:

1. Claude Code가 fix 커밋을 만들어 push한다.
2. **먼저 자동 반응이 왔는지 확인한다.** Codex는 findings가 있으면 인라인 리뷰 코멘트를 남기고, **findings가 없으면 코멘트 없이 PR에 👍(`+1`) 리액션만 남긴다.** 그래서 반드시 두 엔드포인트를 모두 확인해야 한다 — 코멘트만 확인하면 "리뷰는 자동으로 됐는데 이상 없음"인 경우를 "자동 트리거 실패"로 잘못 판단하게 된다.

```bash
# 인라인 리뷰 코멘트 (findings가 있을 때)
curl -s -H "Authorization: Bearer $BID_RADAR_GH_PR_READ_TOKEN" \
  -H "Accept: application/vnd.github+json" \
  https://api.github.com/repos/nini98/bid-radar/pulls/{PR번호}/comments

# PR(issue) 리액션 (findings가 없을 때 👍만 남음)
curl -s -H "Authorization: Bearer $BID_RADAR_GH_PR_READ_TOKEN" \
  -H "Accept: application/vnd.github+json" \
  https://api.github.com/repos/nini98/bid-radar/issues/{PR번호}/reactions
```

push 시각 이후의 `created_at`을 가진 코멘트나 리액션이 있는지로 판단한다. 실측 기준 자동 반응은 보통 4~9분 안에 온다.

3. 수 분(10분 이상) 기다려도 코멘트도 리액션도 없으면, 그때 `gh pr comment {PR번호} --body "@codex review"`로 재리뷰를 직접 트리거한다 (`gh pr comment`는 `.claude/settings.json`에서 막혀있지 않음). 트리거 후에도 위 2번 방식으로 확인한다.
4. findings가 있으면 다시 1번으로 돌아가고, 없으면(코멘트 없음 + 👍 리액션 확인) 종료한다.

---

## 6. 절대 금지

- 토큰 값을 이 문서나 다른 어떤 파일에도 기록하지 않는다.
- 토큰 값을 커밋, PR 코멘트, 채팅 로그에 붙여넣지 않는다.
- `.claude/settings.json`의 `Bash(gh api *)` deny를 이 목적으로 풀지 않는다.
- 이 토큰에 Pull requests: Read-only 이외의 권한을 추가하지 않는다.
