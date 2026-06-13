from locust import HttpUser, between, task


class PromotionUser(HttpUser):
    wait_time = between(1, 3)

    def on_start(self):
        response = self.client.post(
            "/api/v1/auth/login",
            json={"username": "load-user", "password": "load-password"},
            name="/api/v1/auth/login",
        )
        self.token = response.json().get("token") if response.ok else None
        self.headers = {"Authorization": f"Bearer {self.token}"} if self.token else {}

    @task(3)
    def check_status(self):
        self.client.get(
            "/api/v1/health/status/user-123",
            headers=self.headers,
            name="/api/v1/health/status/{anonymousId}",
        )

    @task(2)
    def report_contact(self):
        self.client.post(
            "/api/v1/health/contact",
            json={
                "sourceUserId": "user-123",
                "targetUserId": "user-456",
                "durationMinutes": 12,
            },
            headers=self.headers,
            name="/api/v1/health/contact",
        )

    @task
    def submit_survey(self):
        self.client.post(
            "/api/v1/surveys",
            json={
                "anonymousId": "user-123",
                "hasFever": False,
                "hasCough": False,
                "responses": {"symptoms": []},
            },
            headers=self.headers,
            name="/api/v1/surveys",
        )
