# Configuración de Branch Protection Rules

Este documento describe las reglas de protección de branches que se deben configurar en GitHub.

## Cómo configurar

1. Ir a **Settings** → **Branches** en el repositorio de GitHub
2. Click en **Add branch protection rule**
3. Configurar según las instrucciones de abajo

---

## Branch: `main`

### Branch name pattern
```
main
```

### Reglas recomendadas

- [x] **Require a pull request before merging**
  - [x] Require approvals: `1`
  - [x] Dismiss stale pull request approvals when new commits are pushed
  - [x] Require approval of the most recent reviewable push

- [x] **Require status checks to pass before merging**
  - [x] Require branches to be up to date before merging
  - Status checks requeridos:
    - `Build & Test`
    - `Validate Branch Flow`

- [x] **Require conversation resolution before merging**

- [x] **Do not allow bypassing the above settings**

- [ ] **Allow force pushes** - ❌ NO habilitar

- [ ] **Allow deletions** - ❌ NO habilitar

---

## Branch: `develop`

### Branch name pattern
```
develop
```

### Reglas recomendadas

- [x] **Require a pull request before merging**
  - [x] Require approvals: `1`
  - [x] Dismiss stale pull request approvals when new commits are pushed

- [x] **Require status checks to pass before merging**
  - [x] Require branches to be up to date before merging
  - Status checks requeridos:
    - `Build & Test`
    - `Validate Branch Flow`

- [x] **Require conversation resolution before merging**

- [ ] **Allow force pushes** - ❌ NO habilitar

- [ ] **Allow deletions** - ❌ NO habilitar

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
| `fix/*` | `develop` | Corrección de bugs en desarrollo |
| `release/*` | `main` | Preparación de nueva versión |
| `hotfix/*` | `main` | Correcciones urgentes en producción |
| `backport/*` | `develop` | Sincronización automática de main → develop |
| `docs/*` | `develop` | Cambios de documentación |

### Ejemplo de flujo completo

1. **Desarrollar feature:**
   ```bash
   git checkout develop
   git pull origin develop
   git checkout -b feature/nueva-funcionalidad
   # ... hacer cambios ...
   git push origin feature/nueva-funcionalidad
   # Crear PR → develop
   ```

2. **Crear release:**
   ```bash
   git checkout develop
   git pull origin develop
   git checkout -b release/v1.0.0
   # ... ajustes finales si es necesario ...
   git push origin release/v1.0.0
   # Crear PR → main
   ```

3. **Después del merge a main:**
   - Se crea automáticamente un tag `v1.0.0`
   - Se crea automáticamente un PR de backport a `develop`
   - Revisar y mergear el backport PR

### Hotfix (corrección urgente en producción)

```bash
git checkout main
git pull origin main
git checkout -b hotfix/correccion-critica
# ... hacer fix ...
git push origin hotfix/correccion-critica
# Crear PR → main
# Después del merge, se creará automáticamente el backport a develop
```

