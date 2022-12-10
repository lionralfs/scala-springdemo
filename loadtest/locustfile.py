from locust import HttpUser, between, task

class WebsiteUser(HttpUser):
    @task
    def hello_world(self):
        self.client.get("/hello")
