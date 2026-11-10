import { forwardRef } from 'react';
import { useAppState } from '../../context/AppStateContext.jsx';
import { createPost } from '../../services/posts.js';
import Avatar from '../common/Avatar.jsx';
import Button from '../common/Button.jsx';
import PostForm from './PostForm.jsx';
import './Composer.css';

const Composer = forwardRef(function Composer({ kind, onPosted }, textareaRef) {
  const { me, loggedIn, authLoading } = useAppState();
  return (
    <div className="composer" id="compose">
      <Avatar initial={me.initial} color={me.av} src={me.profileImage || undefined} />
      <div className="composer__body">
        {authLoading ? <p role="status">로그인 상태를 확인하고 있어요.</p>
          : <PostForm ref={textareaRef} kind={kind} onSave={async (body) => {
            if (!loggedIn) throw new Error('게시하려면 로그인해 주세요.');
            const post = await createPost(body);
            onPosted?.(post);
          }} />}
        {!authLoading && !loggedIn && <Button to="/login" variant="ghost" size="sm">로그인하고 게시하기</Button>}
      </div>
    </div>
  );
});
export default Composer;
