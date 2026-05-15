# Branch Protection Rules

## Configurar en GitHub

Settings → Branches → Add classic branch protection rule

---

## Reglas de Flujo de Branches

### ¿Qué puede ir a `main`?
| Rama | Permitido |
|------|-----------|
| `release/*` | ✅ Sí |
| `hotfix/*` | ✅ Sí |
| `develop` | ✅ Sí |
| `feature/*` | ❌ NO |
| `fix/*` | ❌ NO |
| `backport/*` | ❌ NO |

### ¿Qué puede ir a `develop`?
| Rama | Permitido |
|------|-----------|
| `feature/*` | ✅ Sí |
| `fix/*` | ✅ Sí |
| `backport/*` | ✅ Sí |
| `docs/*` | ✅ Sí |
| `release/*` | ❌ NO |
| `hotfix/*` | ❌ NO |

---

## Branch: `main`

**Pattern:** `main`

### Reglas:
- [x] Require a pull request before merging
  - [x] Require approvals: 1
  - [x] Dismiss stale approvals when new commits are pushed
- [x] Require status checks to pass before merging
  - Required checks: `Build & Test`, `Validate Branch Flow`
- [x] Require conversation resolution before merging
- [ ] Allow force pushes ❌
- [ ] Allow deletions ❌

---

## Branch: `develop`

**Pattern:** `develop`

### Reglas:
- [x] Require a pull request before merging
  - [x] Require approvals: 1
- [x] Require status checks to pass before merging
  - Required checks: `Build & Test`, `Validate Branch Flow`
- [ ] Allow force pushes ❌
- [ ] Allow deletions ❌

---

## Flujo de trabajo

```
feature/xxx ─────► develop ─────► release/vX.X.X ─────► main
                      ▲                                   │
                      │                                   │
                      └───────── backport (auto) ─────────┘
```

### Tipos de branches

| Prefijo | Destino | Descripción |
|---------|---------|-------------|
| `feature/*` | `develop` | Nuevas funcionalidades |
| `fix/*` | `develop` | Corrección de bugs |
| `release/*` | `main` | Nueva versión |
| `hotfix/*` | `main` | Correcciones urgentes |
| `backport/*` | `develop` | Sincronización auto |

### Ejemplo

```bash
# Feature
git checkout develop && git pull
git checkout -b feature/mi-feature
git push origin feature/mi-feature
# Crear PR → develop

# Release
git checkout develop && git pull
git checkout -b release/v1.0.0
git push origin release/v1.0.0
# Crear PR → main
# Auto: tag v1.0.0 + backport PR
```

