# Render.com Free Deployment Guide (Option B)

This guide completely avoids cloud server charges by bypassing Render's "Background Worker" limitations using a custom "Super Container."

## Step 1: Push Your Code to GitHub
Render requires a GitHub (or GitLab) repository to trigger automatic builds.
1. Create a **public** repository on GitHub.
2. In your local terminal, run:
   ```bash
   git init
   git add .
   git commit -m "Deployment setup"
   git branch -M main
   git remote add origin https://github.com/YOUR_USERNAME/YOUR_REPO_NAME.git
   git push -u origin main
   ```

## Step 2: Deploy on Render
1. Go to [Render.com](https://render.com) and create a free account.
2. Click **New +** and select **Web Service**.
3. Under *Connect an existing repository*, choose the GitHub repo you just created.
4. Fill out the configuration settings exactly as follows:
   - **Name**: `traffic-shaping-simulator`
   - **Runtime**: `Docker`
   - **Dockerfile Path**: `Dockerfile.render` ⬅️ *(Important! Do not let it use standard Dockerfile)*
   - **Instance Type**: `Free`
5. Advanced Settings (Optional but recommended):
   - Add Environment Variable: `PORT` = `8080` (this maps Render's traffic correctly).
6. Click **Create Web Service**.

## Step 3: Success!
Render will automatically download your code, build the Java binaries, and execute the `render-start.sh` orchestrator. The console logs will show the Spring Boot server starting, followed 10 seconds later by the Traffic Clients booting up. 

Your dashboard will be instantly viewable at the SSL-secured link Render provides you (e.g. `https://traffic-shaping-simulator-xxx.onrender.com`).
