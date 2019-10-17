import { CONFLUENCE_BASE, CONFLUENCE_USER, CONFLUENCE_PSWD } from './constants';
import { exec } from 'child_process';
import axios from 'axios';
import kill from 'tree-kill';

export async function delay(time) {
   return new Promise((resolve) => {
       setTimeout(resolve, time)
   });
}

export async function waitForConfluenceToStart(){
  let text = [];
  return new Promise((resolve, reject) => {
    const server = exec('atlas-run', {
      cwd: `${process.cwd()}/..`,
      shell: '/bin/bash',
    });
    server.stdout.on('data', (data) => {
      const d = data.split(/\r?\n/);
      if (d.length > 0){
        text.push(d[0]);
      }
      for (let i = 1; i < d.length; i++){
        //console.log(`stdout: ${text.join('')}`);
        if (text.join('').indexOf('confluence started successfully') >= 0){
          resolve(server);
        }
        text = [];
        text.push(d[i]);
      }
    });
    server.stderr.on('data', (data) => {});
    server.on('close', (code) => {
      reject();
    });
  });
}

export async function waitForConfluenceToStop(server){
  server.kill();
  return new Promise((resolve) => {
    const server = exec("ps -ef | grep '1990/confluence' | grep -v grep | awk '{print $2}'", (err, stdout, stderr) => {
      kill(stdout.trim(), 'SIGKILL');
      resolve();
    });
  });
}

export async function putPageTitleAndBody(pageId, version, title, body){
  const url = `${CONFLUENCE_BASE}/rest/api/content/${pageId}`;
  return axios.put(url,
    {
      id: `${pageId}`,
      type: 'page',
      title: title,
      version: {
        number: version
      },
      body: {
        storage: {
          value: body,
          representation: 'storage'
        }
      }
    },
    {
      auth: {
        username: CONFLUENCE_USER,
        password: CONFLUENCE_PSWD
      },
      headers: {
        'Content-Type': 'application/json',
        'x-atlassian-token': 'no-check'
      }
    }
  );
}
