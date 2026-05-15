# 🚀 Git Workflow Guide - Examia Backend

## 📖 Overview

This project uses a structured Git flow with automated CI/CD pipelines:
- `main` → Production releases
- `develop` → Integration branch
- `feature/*` → Feature development
- `release/*` → Release preparation
- `hotfix/*` → Emergency fixes

---

## 🔄 Workflow

### 1️⃣ Starting a Feature

```bash
# Update develop with latest changes
git checkout develop
git pull origin develop

# Create feature branch
git checkout -b feature/descriptive-name
```

**Branch naming convention**: `feature/what-you-are-doing`
- ✅ `feature/add-auth-endpoint`
- ✅ `feature/fix-null-pointer-exception`
- ❌ `feature-auth` (use slash)
- ❌ `add-endpoint` (missing "feature" prefix)

### 2️⃣ Work on Feature

```bash
# Make changes and commit
git add .
git commit -m "feat: add user authentication endpoint"

# Push to create PR
git push origin feature/descriptive-name
```

**Commit message format** (Conventional Commits):
- `feat:` - New feature
- `fix:` - Bug fix
- `docs:` - Documentation
- `style:` - Code style (formatting, etc.)
- `refactor:` - Code refactor without feature changes
- `perf:` - Performance improvement
- `test:` - Test additions/updates
- `chore:` - Build process, dependencies, etc.

**Examples:**
- ✅ `feat: add JWT token validation`
- ✅ `fix(database): resolve connection pool leak`
- ✅ `test: add unit tests for UserService`
- ❌ `added endpoint` (no type)
- ❌ `bug fix` (too vague)

### 3️⃣ Create Pull Request

1. Go to GitHub
2. Click "Create Pull Request"
3. Ensure:
   - Base branch: `develop` ✅
   - Title follows Conventional Commits format
   - Description includes related issues
4. Submit PR

**Automated checks:**
- ✅ Branch name validation
- ✅ PR title validation
- ✅ Maven build succeeds
- ✅ Tests pass
- ✅ Code quality checks

### 4️⃣ Code Review

- Respond to review feedback
- Push additional commits (no force push)
- Once approved → Ready to merge

```bash
# Update with latest develop if needed
git fetch origin
git rebase origin/develop
git push --force-with-lease origin feature/descriptive-name
```

### 5️⃣ Merge to Develop

- Click "Squash and merge" or "Create a merge commit"
- Delete branch after merge
- Feature is now in `develop` ✅

---

## 🎯 Releases

### Creating a Release

```bash
# Create release branch from develop
git checkout develop
git pull origin develop
git checkout -b release/v1.2.0
```

**Version format**: `release/vX.Y.Z` (semantic versioning)
- `release/v1.0.0` - Major release
- `release/v1.2.0` - Minor release
- `release/v1.2.3` - Patch release

### Release PR

```bash
git push origin release/v1.2.0
```

1. Create PR to `main`
2. Final testing & review (2 approvals needed)
3. Merge to main

### Automated Actions

When release PR merges to `main`:

1. 📦 **GitHub Release** is created automatically
2. 🏷️ **Tag** `v1.2.0` is added
3. 🔄 **Backport PR** is created automatically:
   - Title: `[Backport] Sync main → develop`
   - Syncs all release changes back to develop
   - Auto-merges to keep branches in sync

---

## 🚨 Hotfixes

### Creating a Hotfix

```bash
# Branch from main (not develop!)
git checkout main
git pull origin main
git checkout -b hotfix/critical-fix-description
```

**Branch naming**: `hotfix/what-you-are-fixing`
- ✅ `hotfix/database-connection-crash`
- ✅ `hotfix/token-expiration-bug`

### Hotfix PR

```bash
git push origin hotfix/critical-fix-description
```

1. Create PR to `main` (⚠️ main, not develop)
2. Urgent review (1 approval)
3. Merge to main

### Automated Actions

When hotfix PR merges to `main`:

1. 🏷️ **No version tag** (hotfix goes to next release)
2. 🔄 **Backport PR** is created automatically:
   - Branches: `main` → `develop`
   - Cherry-picks the fix
   - Syncs develop immediately
3. 💬 Comment on original PR

---

## 📊 Branch Status

### Current Branches

```
main (production)
  └─ release/v1.2.0

develop (integration)
  ├─ feature/add-auth-endpoint
  ├─ feature/improve-caching
  └─ [auto-backports from main]

hotfix branches
  └─ hotfix/database-crash
```

### Check Branch Protection Rules

**Main:**
- 2 approvals required
- GitHub Actions checks must pass
- CODEOWNERS reviews required

**Develop:**
- 1 approval required
- GitHub Actions checks must pass

---

## 🛠️ Common Commands

```bash
# View all branches
git branch -a

# Sync your local branch
git fetch origin
git rebase origin/develop

# Clean up after merge
git branch -d feature/done
git push origin --delete feature/done

# View commit history
git log --oneline --graph --all

# Check what branch you're on
git status

# Build with Maven
mvn clean install
mvn clean verify

# Run tests
mvn test
```

---

## ⚠️ Important Rules

### ❌ Never do this

- Push directly to `main` or `develop`
- Force push to `main`, `develop`, or release branches
- Merge without PR/review
- Commit to branches other than `feature/*`, `release/*`, `hotfix/*`

### ✅ Always do this

- Create PRs for any code changes
- Use meaningful branch names
- Follow commit message conventions
- Address review feedback
- Delete branches after merge
- Keep commits clean and logical

---

## 🔄 Automatic Backports Explained

### Release Backport (main → develop)

**When:** Release PR merges to main  
**What:** Automatic PR created to sync main changes back to develop  
**Why:** Develop stays up-to-date with production releases  
**Label:** `backport` + `automated`

```
main ──release/v1.2.0── (merge)
         │
         └──→ auto-backport
                    │
                develop (updated)
```

### Hotfix Backport (main → develop)

**When:** Hotfix PR merges to main  
**What:** Cherry-picks the fix commit to develop  
**Why:** Urgent fixes don't get lost; both branches have the fix  
**Label:** `backport` + `hotfix` + `automated`

```
main ──hotfix/auth── (merge)
         │
         └──→ cherry-pick
                    │
                develop (synced)
```

---

## 🚀 Getting Started

1. Clone repo: `git clone <url>`
2. Checkout develop: `git checkout develop`
3. Pull latest: `git pull origin develop`
4. Create feature: `git checkout -b feature/your-feature`
5. Make changes, commit, push
6. Create PR on GitHub
7. Let CI validate
8. Get review approval
9. Merge! 🎉

---

## ❓ Questions?

- Check GitHub PR for CI feedback
- Review your commit messages
- Verify branch naming
- Check that base branch is correct

Good luck! 🚀
