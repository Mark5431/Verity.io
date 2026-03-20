"""Minimal in-process metrics helpers for decision-service observability."""

from threading import Lock

from fastapi.responses import PlainTextResponse


class MetricsRegistry:
    """Track simple counters and latency aggregates for API decisions."""

    def __init__(self) -> None:
        self._lock = Lock()
        self._decision_requests_total = 0
        self._fallback_decisions_total = 0
        self._action_counts = {
            "approve": 0,
            "hold": 0,
            "review": 0,
            "block": 0,
        }
        self._latency_sum_ms = 0.0

    def record_decision(self, action: str, latency_ms: float, fallback_used: bool) -> None:
        """Record one completed decision for metrics export."""
        with self._lock:
            self._decision_requests_total += 1
            self._action_counts[action] = self._action_counts.get(action, 0) + 1
            self._latency_sum_ms += latency_ms
            if fallback_used:
                self._fallback_decisions_total += 1

    def render(self) -> str:
        """Render the current metrics registry in Prometheus text format."""
        with self._lock:
            request_count = self._decision_requests_total
            avg_latency_ms = self._latency_sum_ms / request_count if request_count else 0.0
            action_lines = "\n".join(
                f'fraud_decision_action_total{{action="{action}"}} {count}'
                for action, count in self._action_counts.items()
            )
            return (
                "# HELP fraud_decision_requests_total Total decision requests\n"
                "# TYPE fraud_decision_requests_total counter\n"
                f"fraud_decision_requests_total {request_count}\n"
                "# HELP fraud_decision_fallback_total Total fallback-to-rules decisions\n"
                "# TYPE fraud_decision_fallback_total counter\n"
                f"fraud_decision_fallback_total {self._fallback_decisions_total}\n"
                "# HELP fraud_decision_action_total Decision counts by action\n"
                "# TYPE fraud_decision_action_total counter\n"
                f"{action_lines}\n"
                "# HELP fraud_decision_latency_avg_ms Average decision latency in milliseconds\n"
                "# TYPE fraud_decision_latency_avg_ms gauge\n"
                f"fraud_decision_latency_avg_ms {avg_latency_ms:.3f}\n"
            )


_METRICS_REGISTRY = MetricsRegistry()


def record_decision_metric(action: str, latency_ms: float, fallback_used: bool) -> None:
    """Record a single decision outcome in the shared registry."""
    _METRICS_REGISTRY.record_decision(
        action=action,
        latency_ms=latency_ms,
        fallback_used=fallback_used,
    )


def get_metrics() -> PlainTextResponse:
    """Return the shared metrics registry as a plain-text HTTP response."""
    return PlainTextResponse(
        content=_METRICS_REGISTRY.render(),
        media_type="text/plain; version=0.0.4",
    )
