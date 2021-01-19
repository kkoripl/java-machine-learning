import {Injectable} from "@angular/core";

@Injectable()
export class ApiService {
  constructor() {}

  private apiBase(): string {
    return '/api';
  }

  private handwritingBase(): string {
    return this.apiBase() + '/handwritting';
  }

  handWritingRecognitionUrl(): string {
    return this.handwritingBase() + '/recognize';
  }
}
