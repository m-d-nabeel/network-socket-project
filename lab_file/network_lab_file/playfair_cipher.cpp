#include <bits/stdc++.h>
using namespace std;

string encode(string key) {
  string new_key = "";
  for (int i = 0; i < key.length(); i++) {
    if ((key[i] == key[i - 1]) || (key[i - 1] == 'i' && key[i] == 'j')) {
      new_key += 'x';
    } else if (key[i] == 'j') {
      new_key += 'i';
    } else {
      new_key += +key[i];
    }
  }

  return new_key;
}

string enc_dec(string new_text, char matrix[5][5]) {
  unordered_map<char, pair<int, int>> mp;
  for (int i = 0; i < 5; i++) {
    for (int j = 0; j < 5; j++) {
      if (new_text.find(matrix[i][j]) != string::npos) {
        mp[matrix[i][j]] = {i, j};
      }
    }
  }

  string encrypt = "";
  for (int i = 0; i < new_text.length(); i += 2) {
    int r1 = mp[new_text[i]].first;
    int r2 = mp[new_text[i + 1]].first;
    int c1 = mp[new_text[i]].second;
    int c2 = mp[new_text[i + 1]].second;
    encrypt += matrix[r1][c2];
    encrypt += matrix[r2][c1];
  }

  return encrypt;
}

void mat(char matrix[5][5], string new_key) {
  int freq[26] = {0};
  int flag = 0;
  int k = 0;
  for (int i = 0; i < 5; i++) {
    for (int j = 0; j < 5; j++) {
      freq[new_key[k] - 'a']++;
      matrix[i][j] = new_key[k];
      k++;
      if (k == new_key.length()) {
        flag = 1;
        break;
      }
    }
    if (flag == 1) {
      break;
    }
  }

  int row = new_key.length() / 5, col = new_key.length() % 5;
  k = 0;
  for (int i = row; i < 5; i++) {
    for (int j = col; j < 5; j++) {
      if (freq[k] == 0 && static_cast<char>('a' + k) != 'j') {
        matrix[i][j] = static_cast<char>('a' + k);
      } else {
        j--;
      }
      k++;
    }
    col = 0;
  }

  for (int i = 0; i < 5; i++) {
    for (int j = 0; j < 5; j++) {
      cout << matrix[i][j] << " ";
    }
    cout << endl;
  }
}

int main() {
  char matrix[5][5];
  string key;
  cout << "Enter the key : ";
  cin >> key;

  string new_key = encode(key);
  cout << "Your Key is : " << new_key << endl;

  mat(matrix, new_key);

  cout << "Enter the string : ";
  string text;
  cin >> text;

  // New Text generated
  string new_text = encode(text);
  if (new_text.length() % 2 != 0) {
    new_text += 'x';
  }

  cout << endl;
  cout << "New text is : " << new_text << endl;

  // Encryption
  string encrypt = enc_dec(new_text, matrix);
  cout << "Encrypt : " << encrypt << endl;

  // Decryption
  string decrypt = enc_dec(encrypt, matrix);
  cout << "Decrypt : " << decrypt << endl;

  return 0;
}