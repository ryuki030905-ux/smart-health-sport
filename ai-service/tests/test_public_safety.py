import unittest
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
MAIN_FILE = ROOT / "main.py"


class PublicSafetyTest(unittest.TestCase):
    def test_debug_routes_are_guarded_by_flag(self):
        source = MAIN_FILE.read_text(encoding="utf-8")

        self.assertIn('DEBUG_VERBOSE = os.getenv("DEBUG_VERBOSE", "false")', source)
        self.assertIn('if DEBUG_VERBOSE:', source)
        self.assertIn('@app.get("/debug/status")', source)
        self.assertIn('@app.post("/debug/llm")', source)

    def test_cors_defaults_are_limited_to_local_origins(self):
        source = MAIN_FILE.read_text(encoding="utf-8")

        self.assertIn('ALLOWED_ORIGINS = [origin.strip() for origin in os.getenv("ALLOWED_ORIGINS", "http://localhost:5173,http://127.0.0.1:5173")', source)
        self.assertIn('ALLOW_CREDENTIALS = os.getenv("ALLOW_CREDENTIALS", "false")', source)
        self.assertNotIn('allow_origins=["*"]', source)


if __name__ == "__main__":
    unittest.main()
