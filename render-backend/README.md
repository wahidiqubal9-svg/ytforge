# YTForge render backend

A private GitHub repository that renders YTForge projects with HyperFrames on a
GitHub-hosted runner.

## Setup

1. Create a **private** repository under any GitHub account, for example
   `your-account/ytforge-render`.
2. Copy this directory's `.github/workflows/render.yml` into that repository.
3. Enable Actions for the repository.
4. In YTForge Settings, set:
   - Render repo: `your-account/ytforge-render`
   - Render token: a fine-grained personal access token for that repository
5. Token permissions:
   - **Contents: Read and write** (the app uploads `jobs/<id>/project.zip`)
   - **Actions: Read and write** (dispatch + read runs/artifacts)
   - **Metadata: Read**

## Contract

The app uploads a zip to:

```
jobs/<job_id>/project.zip
```

The zip root must contain `index.html` and any local assets. The app dispatches:

```
workflow: render.yml
ref: main
inputs: { "job_id": "<job_id>" }
```

The workflow uploads an artifact named `render-<job_id>` containing:

- `video-720p.mp4`
- `preview-360p.mp4`
- `thumbnail.jpg`

The app polls runs by the run name `ytforge-render-<job_id>`, downloads the
artifact with its token, and verifies the video with Android's player.

## Cost

Private-repo Actions minutes are limited by the account plan. A 10-second 720p
sample takes about one minute including setup; a 60-second video is roughly two
to three minutes including runner startup.
