import { useState, useEffect } from 'react';

export function toFullScreen(dom = document.documentElement) {
  if (dom.requestFullscreen) {
    dom.requestFullscreen();
  } else if (dom.webkitRequestFullscreen) {
    dom.webkitRequestFullscreen();
  } else if (dom.mozRequestFullScreen) {
    dom.mozRequestFullScreen();
  } else {
    dom.msRequestFullscreen();
  }
}

export function exitFullScreen() {
  if (document.exitFullscreen) {
    document.exitFullscreen();
  } else if (document.msExitFullscreen) {
    document.msExitFullscreen();
  } else if (document.mozCancelFullScreen) {
    document.mozCancelFullScreen();
  } else if (document.webkitExitFullscreen) {
    document.webkitExitFullscreen();
  }
}

export function getCurrentFullScreen() {
  const isFullScreen = document.webkitFullscreenElement
    || document.mozFullScreenElement
    || document.msFullscreenElement;
  return Boolean(isFullScreen);
}

export function addFullScreenEventListener(handleChangeFullScreen) {
  document.addEventListener('fullscreenchange', handleChangeFullScreen);
  document.addEventListener('webkitfullscreenchange', handleChangeFullScreen);
  document.addEventListener('mozfullscreenchange', handleChangeFullScreen);
  document.addEventListener('MSFullscreenChange', handleChangeFullScreen);
}

export function removeFullScreenEventListener(handleChangeFullScreen) {
  document.removeEventListener('fullscreenchange', handleChangeFullScreen);
  document.removeEventListener('webkitfullscreenchange', handleChangeFullScreen);
  document.removeEventListener('mozfullscreenchange', handleChangeFullScreen);
  document.removeEventListener('MSFullscreenChange', handleChangeFullScreen);
}
