curl -H "Content-Type: application/json" \
    -X POST \
    -d "{\"content\": \"🚨 1team 백엔드 배포가 성공적으로 완료되었습니다!\"}" \
    $DISCORD_WEBHOOK_URL
exit 0