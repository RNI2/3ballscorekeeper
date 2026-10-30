"""Validate the portable contract and golden examples (requires Python jsonschema)."""
import json
from pathlib import Path
from jsonschema import Draft202012Validator

root = Path(__file__).resolve().parents[1]
schema = json.loads((root / "docs/contract/game-record-v1.schema.json").read_text())
Draft202012Validator.check_schema(schema)
validator = Draft202012Validator(schema)
examples = sorted((root / "records/src/test/resources/contract").glob("*.json"))
for path in examples:
    record = json.loads(path.read_text())
    validator.validate(record)
    invalid = dict(record, storageSchemaVersion=99)
    assert list(validator.iter_errors(invalid)), "Future schema versions must be rejected"
    invalid = dict(record, syncEligible=True)
    assert list(validator.iter_errors(invalid)), "Casual records cannot enable synchronization"
print(f"{len(examples)} portable JSON fixtures validated; future versions and sync eligibility rejected.")
